# Plano: Maquininhas, bandeiras e tarifas com vigência

## Problema
Cadastrar maquininhas de pagamento. Cada maquininha habilita um subconjunto de 4 tipos de pagamento
(Dinheiro, Pix, Cartão de Débito, Cartão de Crédito) e cada tipo pode ter tarifa (%), variando por
bandeira e parcelas. Tarifas mudam com o tempo: cada tarifa tem **data de início de vigência** e o
histórico **nunca é sobrescrito**.

## Regras de negócio
| Tipo | Bandeira | Parcelas | Tarifa |
|---|---|---|---|
| Dinheiro | não | não | opcional: o usuário decide se há tarifa (sem tarifa = 0%) |
| Pix | não | não | única (%) |
| Débito | sim | não | por bandeira (%) |
| Crédito | sim | sim, **cada parcela individual** (1x, 2x, 3x…), sem faixas | uma tarifa (%) por bandeira + parcela |

- Bandeiras: cadastradas **dentro de cada maquininha** (não há catálogo global). Cada maquininha mantém sua própria lista de nomes de bandeira, já que maquininhas diferentes podem aceitar conjuntos diferentes de bandeiras.
- No cadastro da maquininha, o usuário digita o nome da bandeira diretamente nas seções de Débito/Crédito; o nome vira o identificador da bandeira naquela maquininha.
- Tarifa digitada em %, armazenada como inteiro em **basis points** (2,49% = 249). Nunca `Float`/`Double`; na UI `BigDecimal`. Validar 0 a 100% e no máximo 2 casas.
- Cada tarifa tem `effectiveFrom` (data de início). A tarifa válida numa data D é a de maior `effectiveFrom <= D` para a mesma chave (maquininha + tipo + bandeira + nº da parcela).
- **Histórico imutável**: alterar tarifa = inserir novo registro com nova vigência. Sem update/delete (reforçado nas regras do Firestore). Se o usuário esquecer de registrar na data certa, pode inserir um registro com data retroativa; a consulta por data resolve.
- Mesma chave + mesma `effectiveFrom` é rejeitada (evita ambiguidade); corrigir = nova data.
- Vendas futuras devem gravar **snapshot** da tarifa aplicada (bps, bandeira, parcelas, maquininha), conforme o princípio do projeto de não reescrever histórico. A implementação da venda pertence a outro fluxo e não faz parte deste planejamento.
- Desativar maquininha = `active=false` (arquivar), nunca apagar se houver tarifas ou vendas.

## Modelo de dados (Firestore, dentro da loja)
- `stores/{storeId}/cardBrands/{brandId}`: `name`, `active`, `createdAt`
- `stores/{storeId}/paymentTerminals/{terminalId}`: `name`, `active`, `enabledMethods` (lista de CASH/PIX/DEBIT/CREDIT), `enabledBrandIds`
- `stores/{storeId}/paymentTerminals/{terminalId}/fees/{feeId}` (append-only):
  `method`, `brandId?`, `installments?` (nº exato da parcela), `rateBps`, `effectiveFrom`, `createdAt`
- `firestore.rules`: fees só `create`/`read` (sem `update`/`delete`); demais com acesso restrito à sessão da loja.

Camadas (seguindo INSTRUCTION.md: models UI separados de request, `strings.xml`, stateful/stateless):
- `domain/payment`: evoluir `PaymentMethods` para CASH, PIX, DEBIT, CREDIT (hoje há CASH, PIX, CARD); `PaymentTerminalModel` (com `brands: List<String>` próprio da maquininha), `TerminalFeeModel`, `PaymentFailure`; use cases (CRUD de maquininha, `AddTerminalFeeUseCase`, `GetTerminalFeesUseCase`, `ResolveFeeForDateUseCase` — puro e testável).
- `data/payment`: `*Request` (bps `Int`, data ISO), `*Record`, mappers, `FirebasePayment*DataSource`, repositório, módulo Hilt.
- `ui/screens/...`: telas abaixo. Atualizar `PaymentMethodsUi` (nomes via `stringResource`, e corrigir o encoding quebrado atual).

## UX (vínculo no cadastro com chips)
No cadastro da maquininha, tudo é vinculado por **chips** (Material 3 `FilterChip`): selecionar um chip revela as opções dependentes abaixo; desselecionar oculta (o histórico de tarifas permanece).

1. **Configurações → Maquininhas** (ver `docs/proposta-tarifas-e-pagamento.svg`): lista + "Adicionar".
2. **Cadastro/edição da maquininha**:
  - Nome.
  - Chips de tipo: `Dinheiro` `Pix` `Débito` `Crédito` (multisseleção).
  - **Dinheiro** selecionado → opção "Cobra tarifa?" (chip/switch); se sim, tarifa % + início da vigência.
  - **Pix** selecionado → tarifa % + início da vigência.
  - **Débito** selecionado → campo para cadastrar o nome de uma nova bandeira (própria desta maquininha) + chips das bandeiras já cadastradas nela; cada bandeira marcada mostra tarifa % + início.
  - **Crédito** selecionado → mesmo cadastro inline de bandeiras (desta maquininha); cada bandeira marcada permite configurar suas parcelas individualmente. A seleção da parcela deve utilizar um componente **Spinner**, com as parcelas previamente cadastradas para aquela bandeira. Cada combinação de bandeira + parcela possui sua própria tarifa (%) e data de início da vigência. Exemplo: Visa 1x possui uma tarifa, Visa 2x possui outra e Mastercard 1x possui outra. As tarifas nunca são compartilhadas entre parcelas ou bandeiras.
3. **Histórico de tarifas** por tipo/bandeira/parcela: ordenado por vigência, atual destacada, botão "Nova tarifa" (% + data de início). No crédito, o histórico deve ser independente para cada combinação de bandeira e parcela. Nunca edita registro antigo.

## Todos (ordem sugerida)
1. `payment-domain` — modelos de domínio, enum com DEBIT/CREDIT, `ResolveFeeForDate` + testes unitários (vigência, empate, retroativo, bps).
2. `payment-data` — requests/records/mappers, data sources Firestore, repositório, DI, regras do Firestore (fees append-only).
3. `terminal-list-screen` — lista de maquininhas + rota/navegação e entrada em Configurações.
4. `terminal-form-screen` — formulário com chips (FilterChip) de tipo e bandeira (cadastrada inline, por maquininha); tarifas por tipo/bandeira/parcela, seleção de parcelas por Spinner, data de início.
5. `fee-history-screen` — histórico e nova tarifa com vigência.
6. `strings-and-validation` — textos em `strings.xml`, validações (%, datas, parcelas entre 1 e 12 sem duplicidade, duplicidade de vigência), estados vazio/erro/loading, confirmação ao arquivar.
7. `sale-integration` — **fora do escopo deste planejamento**. A implementação de Nova Venda pertence a outro fluxo e será desenvolvida separadamente, consumindo as configurações cadastradas aqui.

Dependências: 2→1; 3,4→2; 5→3; 6→5; 7 transversal. A integração com Nova Venda não faz parte destas dependências.

## Decisões
- Dinheiro: tarifa opcional, definida pelo usuário.
- Crédito: tarifa individual por parcela (sem faixa).
- Bandeiras cadastradas diretamente dentro de cada maquininha (sem catálogo global), pois maquininhas diferentes podem aceitar bandeiras diferentes.
- Bandeiras exibidas somente pelo nome, sem ícones.
- Parcelas de crédito selecionadas por Spinner, utilizando as parcelas previamente cadastradas para cada bandeira.
- Cada combinação de bandeira e parcela possui tarifa e vigência independentes.
- Integração com a Nova Venda: fora do escopo deste planejamento.
- Vínculo no cadastro: componentes de chips.