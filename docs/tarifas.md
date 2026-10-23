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

- Bandeiras: catálogo cadastrado pelo usuário (somente **nome**, sem ícone). Aparecem só nas maquininhas que as habilitarem.
- Tarifa digitada em %, armazenada como inteiro em **basis points** (2,49% = 249). Nunca `Float`/`Double`; na UI `BigDecimal`. Validar 0 a 100% e no máximo 2 casas.
- Cada tarifa tem `effectiveFrom` (data de início). A tarifa válida numa data D é a de maior `effectiveFrom <= D` para a mesma chave (maquininha + tipo + bandeira + nº da parcela).
- **Histórico imutável**: alterar tarifa = inserir novo registro com nova vigência. Sem update/delete (reforçado nas regras do Firestore). Se o usuário esquecer de registrar na data certa, pode inserir um registro com data retroativa; a consulta por data resolve.
- Mesma chave + mesma `effectiveFrom` é rejeitada (evita ambiguidade); corrigir = nova data.
- Vendas futuras devem gravar **snapshot** da tarifa aplicada (bps, bandeira, parcelas, maquininha), conforme o princípio do projeto de não reescrever histórico.
- Desativar maquininha/bandeira = `active=false` (arquivar), nunca apagar se houver tarifas ou vendas.

## Modelo de dados (Firestore, dentro da loja)
- `stores/{storeId}/cardBrands/{brandId}`: `name`, `active`, `createdAt`
- `stores/{storeId}/paymentTerminals/{terminalId}`: `name`, `active`, `enabledMethods` (lista de CASH/PIX/DEBIT/CREDIT), `enabledBrandIds`
- `stores/{storeId}/paymentTerminals/{terminalId}/fees/{feeId}` (append-only):
  `method`, `brandId?`, `installments?` (nº exato da parcela), `rateBps`, `effectiveFrom`, `createdAt`
- `firestore.rules`: fees só `create`/`read` (sem `update`/`delete`); demais com acesso restrito à sessão da loja.

Camadas (seguindo INSTRUCTION.md: models UI separados de request, `strings.xml`, stateful/stateless):
- `domain/payment`: evoluir `PaymentMethods` para CASH, PIX, DEBIT, CREDIT (hoje há CASH, PIX, CARD); `CardBrandModel`, `PaymentTerminalModel`, `TerminalFeeModel`, `PaymentFailure`; use cases (CRUD de bandeira/maquininha, `AddTerminalFeeUseCase`, `GetTerminalFeesUseCase`, `ResolveFeeForDateUseCase` — puro e testável).
- `data/payment`: `*Request` (bps `Int`, data ISO), `*Record`, mappers, `FirebasePayment*DataSource`, repositório, módulo Hilt.
- `ui/screens/...`: telas abaixo. Atualizar `PaymentMethodsUi` (nomes via `stringResource`, e corrigir o encoding quebrado atual).

## UX (vínculo no cadastro com chips)
No cadastro da maquininha, tudo é vinculado por **chips** (Material 3 `FilterChip`): selecionar um chip revela as opções dependentes abaixo; desselecionar oculta (o histórico de tarifas permanece).

1. **Configurações → Maquininhas** (ver `docs/proposta-tarifas-e-pagamento.svg`): lista + "Adicionar".
2. **Configurações → Bandeiras**: lista, adicionar/renomear/arquivar (só nome).
3. **Cadastro/edição da maquininha**:
   - Nome.
   - Chips de tipo: `Dinheiro` `Pix` `Débito` `Crédito` (multisseleção).
   - **Dinheiro** selecionado → opção "Cobra tarifa?" (chip/switch); se sim, tarifa % + início da vigência.
   - **Pix** selecionado → tarifa % + início da vigência.
   - **Débito** selecionado → chips das bandeiras cadastradas; cada bandeira marcada mostra tarifa % + início.
   - **Crédito** selecionado → chips das bandeiras; cada bandeira marcada mostra chips de parcelas (`1x` … `12x`); cada parcela marcada mostra sua tarifa % + início.
4. **Histórico de tarifas** por tipo/bandeira/parcela: ordenado por vigência, atual destacada, botão "Nova tarifa" (% + data de início). Nunca edita registro antigo.
5. **Nova venda** (incluída no escopo): o usuário escolhe a maquininha e depois o método de pagamento (chips); conforme a escolha, habilita as opções seguintes:
   - Dinheiro → (tarifa, se configurada).
   - Pix → tarifa.
   - Débito → chips das bandeiras habilitadas na maquininha.
   - Crédito → bandeira, depois parcela (somente as parcelas com tarifa cadastrada).
   - Tarifa resolvida pela data da venda (`ResolveFee`) e gravada como **snapshot** na venda.
## Todos (ordem sugerida)
1. `payment-domain` — modelos de domínio, enum com DEBIT/CREDIT, `ResolveFeeForDate` + testes unitários (vigência, empate, retroativo, bps).
2. `payment-data` — requests/records/mappers, data sources Firestore, repositório, DI, regras do Firestore (fees append-only).
3. `card-brands-screen` — CRUD de bandeiras (nome apenas).
4. `terminal-list-screen` — lista de maquininhas + rota/navegação e entrada em Configurações.
5. `terminal-form-screen` — formulário com chips (FilterChip) de tipo, bandeira e parcela, tarifas por tipo/bandeira/parcela, data de início.
6. `fee-history-screen` — histórico e nova tarifa com vigência.
7. `strings-and-validation` — textos em `strings.xml`, validações (%, datas, parcelas entre 1 e 12 sem duplicidade, duplicidade de vigência), estados vazio/erro/loading, confirmação ao arquivar.
8. `sale-integration` — Nova Venda: escolher maquininha → método (chips) → bandeira → parcela, com snapshot da tarifa.

Dependências: 2→1; 3,4→2; 5→3,4; 6→5; 7 transversal; 8→6.

## Decisões
- Dinheiro: tarifa opcional, definida pelo usuário.
- Crédito: tarifa individual por parcela (sem faixa).
- Integração com a Nova Venda: incluída (item 8 deixa de ser opcional).
- Vínculo no cadastro: componentes de chips.