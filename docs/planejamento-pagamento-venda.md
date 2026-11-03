# Planejamento: forma de pagamento e tarifa na tela Nova Venda (`AddPurchaseScreen`)

Complementa `docs/tarifas.md` (cadastro de maquininhas/tarifas), que deixou a integração com a venda "fora do escopo". Este documento cobre essa integração.

## Estado atual
- `PaymentMethods` já é `CASH, PIX, DEBIT, CREDIT` (o `CARD` foi removido; `PaymentMethodsUi` e um teste que ainda usavam `CARD` foram ajustados).
- `AddPurchaseScreen` tem hoje um único spinner de forma de pagamento (`PurchaseFormUiState.paymentMethod`), salvo em `PurchaseModel/Request/Record` como `paymentMethod`.
- Já existem: `PaymentTerminalModel` (nome, `enabledMethods`, `brands`), `PaymentMachineModel` (tarifa append-only: terminal + método + bandeira + parcela + `rateBps` + `effectiveFrom`), `GetFilteredCurrentTariffUseCase(tariffs, date)`, `PaymentMachineRepository.getTariffsSnapshot(terminalId)` e `GetTerminalsUseCase`.

## Fluxo de UI (cada campo só habilita após o anterior estar preenchido)
| Forma de pagamento | Campos exibidos (em ordem) |
|---|---|
| Dinheiro | nenhum adicional (a tarifa de dinheiro é buscada no banco, sem input do usuário) |
| Pix | Maquininha |
| Débito | Maquininha → Bandeira |
| Crédito | Maquininha → Bandeira → Parcelas |

Regras:
1. Forma de pagamento é o 1º spinner. Trocar a forma **limpa** maquininha/bandeira/parcela selecionadas.
2. Spinner de Maquininha: só terminais `active` com o método escolhido em `enabledMethods`.
3. Spinner de Bandeira: `terminal.brands` que possuam tarifa para o método (débito/crédito).
4. Spinner de Parcelas (só crédito): parcelas com tarifa cadastrada para aquela maquininha + bandeira.
5. Spinner posterior fica `enabled = false` enquanto o anterior estiver vazio; trocar um spinner limpa os seguintes.
6. Trocar a **data da venda** recalcula a tarifa (e revalida a seleção: se a maquininha/bandeira/parcela não tiver tarifa vigente na nova data, avisar e limpar a seleção afetada).
7. `canSubmit` exige todos os campos obrigatórios da forma escolhida **e** os spinners da forma escolhida preenchidos. Tarifa não é obrigatória: se não houver tarifa vigente, a venda é salva sem taxa (0).
8. Opcional: mostrar a tarifa aplicada (%) e o valor descontado perto do `ProfitCard`.
9. Textos em `strings.xml`, medidas em `dimens.xml`, composable stateless recebendo estado/callbacks (INSTRUCTION.md).

## Resolução da tarifa por data da venda
Já coberta por `GetFilteredCurrentTariffUseCase`: entre as tarifas da mesma chave (terminal + método + bandeira + parcela) escolhe a de maior `effectiveFrom <= saleDate`.
- Data de hoje com última atualização há 1 mês → usa a atualização de 1 mês atrás.
- Data retroativa anterior à última atualização → usa a tarifa anterior (a vigente naquele dia).
- Sem nenhuma tarifa com `effectiveFrom <= saleDate` → sem tarifa: **não bloqueia**; calcula/salva sem taxa (`tariffRateBps = 0`, `tariffAmountCents = 0`, `tariffId = null`).
- **Dinheiro**: não há maquininha na tela. A tarifa vem do banco (tarifas `method = CASH` configuradas em "Cobra tarifa?"), resolvida pela data da venda; sem tarifa cadastrada (provável caso comum) = 0.
- Comparação de datas: normalizar a data da venda para o início/fim do dia local de forma consistente com `effectiveFrom` (que é uma data sem hora), para uma tarifa que começa "hoje" valer para vendas de hoje.

## Cache das tarifas: app inteiro ou só nesta tela?
**Recomendação: carregar só nesta tela (por ViewModel), sem cache global.**
- Tarifas só são usadas aqui (e no cadastro de maquininhas, que já observa o próprio fluxo).
- O Firestore já tem cache offline local; um cache global duplicaria estado e geraria risco de dado desatualizado (viola o princípio "Firestore é fonte de verdade" do INSTRUCTION.md).
- O `AddPurchaseViewModel` carrega, ao abrir: terminais ativos (`GetTerminalsUseCase`) e, ao selecionar a maquininha, as tarifas dela (`getTariffsSnapshot(terminalId)`), mantendo em memória apenas durante a vida da tela. A resolução por data é local (função pura), então mudar data/bandeira/parcela não gera nova consulta.
- Se no futuro outra tela precisar (ex.: relatório de taxas), extrair um repositório com `Flow` compartilhado — não agora.

## Dados salvos com a venda (snapshot imutável)
A venda deve guardar uma **cópia** da tarifa aplicada, pois o histórico nunca pode mudar depois (INSTRUCTION.md). Novos campos em `PurchaseModel` / `PurchaseUiModel` / `PurchaseRequest` / `PurchaseRecord`:

| Campo | Tipo | Observação |
|---|---|---|
| `paymentMethod` | `PaymentMethods` | já existe |
| `terminalId` | `String?` | null em dinheiro |
| `terminalName` | `String?` | snapshot do nome |
| `brand` | `String?` | débito/crédito |
| `installments` | `Int?` | só crédito |
| `tariffRateBps` | `Int` | basis points inteiros (2,49% = 249); 0 quando sem tarifa |
| `tariffId` | `String?` | referência à tarifa usada (null se sem tarifa) |
| `tariffAmountCents` | `Long` | valor da taxa em centavos inteiros, calculado sobre o valor da venda; 0 sem tarifa |

- **Tudo é inteiro no banco**: `rateBps` (`Int`), valores em centavos (`Long`), como já fazem `PurchaseModel.cost`/`salePrice` e `PaymentMachineRequest`. A conversão para a UI (`BigDecimal`, % com 2 casas e `R$`) é feita só no mapper/UI, de forma explícita e sem arredondamento silencioso (INSTRUCTION.md). Sem `Float`/`Double`.
- Cálculo da taxa em inteiros: `amountCents = valorVendaCents * rateBps / 10_000`, com regra de arredondamento definida e testada (ex.: `HALF_UP` via `BigDecimal` interno, resultado convertido de volta a `Long`).
- Compatibilidade: registros antigos não têm os campos → defaults no Record (`null`/0); vendas antigas continuam abrindo. O banco será limpo onde houver `CARD`, então não é necessário tratar `"CARD"` legado no mapper.
- Editar venda: ao editar, **manter o snapshot salvo** a menos que o usuário altere forma de pagamento/maquininha/bandeira/parcela/data; nesse caso recalcular.
- `firestore.rules`: revisar se a validação de `purchases` aceita os novos campos.

## Camadas e arquivos prováveis
- `domain/payment/usecase`: `GetSaleTariffUseCase(terminalId, method, brand, installments, saleDate)` — busca snapshot + `GetFilteredCurrentTariffUseCase`; função pura de cálculo do valor da taxa. Testes unitários.
- `domain/purchase`: estender `PurchaseModel`, `CreatePurchaseUseCase`/update (validar combinações por forma de pagamento).
- `data/purchase/model`: `PurchaseRequest`, `PurchaseRecord` + mapper.
- `ui/screens/addpurchase`: `PurchaseFormUiState` (terminal/brand/installments selecionados + listas de opções + tarifa resolvida), `AddPurchaseViewModel` (`onTerminalSelected`, `onBrandSelected`, `onInstallmentsSelected`, recálculo ao mudar data), `AddPurchaseScreen` (spinners condicionais), `strings.xml`.
- `PurchaseUiModel`/telas Vendas e Home: exibir (opcional) forma de pagamento detalhada e taxa.

## Todos (ordem sugerida)
1. `payment-enum-cleanup` — ✅ `PaymentMethods` com DEBIT/CREDIT; `PaymentMethodsUi` e testes ajustados (feito).
2. `sale-tariff-usecase` — `GetSaleTariffUseCase` + cálculo da taxa + testes (data de hoje, retroativa, sem tarifa, mesma data de vigência).
3. `purchase-model-snapshot` — novos campos em Model/UiModel/Request/Record, mappers (inteiros ↔ BigDecimal), compatibilidade com registros antigos, regras do Firestore.
4. `addpurchase-viewmodel` — estado e eventos dos spinners em cascata, limpeza ao trocar anterior, recálculo ao mudar a data, `canSubmit`; testes do ViewModel.
5. `addpurchase-screen` — spinners condicionais/encadeados (`enabled` conforme anterior), strings/dimens, exibição da tarifa; atualizar `AddPurchaseScreenUiTest`.
6. `edit-flow` — pré-preencher a edição a partir do snapshot salvo; recalcular só se o usuário mudar os campos.
7. `validation-and-states` — loading dos terminais/tarifas, vazio (nenhuma maquininha para o método), erro, mensagem de "sem tarifa vigente nesta data".

Dependências: 3→2; 4→3; 5→4; 6→5; 7 transversal.

## Decisões tomadas
1. Dinheiro: usa a tarifa salva no banco (provavelmente 0).
2. Sem tarifa vigente: calcula e salva sem taxa, sem bloquear.
3. Vendas antigas com `CARD`: o banco será limpo; sem tratamento legado.
4. Valores no banco sempre inteiros (bps e centavos), convertidos para `BigDecimal` só na UI.

## Em aberto
- Dinheiro com várias maquininhas que têm tarifa de dinheiro: qual usar? Sugestão: a vigente mais recente entre todas, ou 0 se nenhuma.
- Taxa fora do lucro bruto (INSTRUCTION.md): só salvar/exibir a taxa, sem alterar o lucro?

