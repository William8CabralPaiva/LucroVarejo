# Lucro Varejo

Aplicativo Android para apoiar o controle de produtos, compras e estoque de um pequeno varejo. O projeto usa Kotlin, Jetpack Compose, Firebase Authentication e Cloud Firestore.

## Banco de dados

O Cloud Firestore e um banco NoSQL orientado a documentos. Os dados ficam separados por loja, usando o UID do Firebase Authentication como identificador:

```text
stores/{storeId}
  members/{uid}
  products/{productId}
  purchases/{purchaseId}
  paymentTerminals/{terminalId}
    tariffs/{tariffId}
users/{uid}/stores/{storeId}   (atalho opcional, ver abaixo)
```

Uma conta (e-mail/senha) pode ter varias lojas. O acesso aos dados de `stores/{storeId}` exige que o UID seja o proprio `storeId` (lojas antigas e a primeira loja da conta) ou exista `stores/{storeId}/members/{uid}` (`role = OWNER`). `users/{uid}/stores/{storeId}` (`displayName`, `role`, `createdAt`) e so o atalho para listar as lojas do usuario: **facilita a leitura, mas nao e necessario** e nunca concede acesso (poderia ser substituido por uma collection group query em `members`); contas antigas ganham esse atalho no primeiro carregamento. Em debug as colecoes se chamam `stores_debug` e `users_debug` (`FirestoreCollections`). A loja ativa fica salva localmente por UID (`ActiveStoreHolder`) e a troca/criacao esta em Configuracoes > Trocar loja. O "esqueci a senha" continua por conta (e-mail), nao por loja. Os demais IDs sao gerados pelo Firestore. Tudo fica em subcolecoes da loja, nao em colecoes globais.

> Atencao: no codigo atual, a colecao `purchases` guarda as **vendas** (tem preco e data de venda e forma de pagamento). No `INSTRUCTION.md`, "compra" e o lote de estoque, que ainda nao existe como colecao.

### Documento da loja: `stores/{storeId}`

| Campo | Tipo | Descricao |
|---|---|---|
| `displayName` | string | Nome de exibicao da loja |
| `email` | string | E-mail de contato informado no cadastro |
| `createdAt` | timestamp | Data de criacao, definida pelo servidor |

### Documento de produto: `stores/{storeId}/products/{productId}`

| Campo | Tipo | Descricao |
|---|---|---|
| `cod` | string | Codigo do produto |
| `name` | string | Nome do produto |
| `cost` | integer | Custo em centavos (por exemplo, `1234` representa R$ 12,34) |
| `description` | string | Descricao do produto |

### Documento de venda: `stores/{storeId}/purchases/{purchaseId}`

| Campo | Tipo | Descricao |
|---|---|---|
| `cod` | string | Codigo do produto |
| `name` | string | Nome do produto no momento da venda |
| `cost` | integer | Custo unitario em centavos |
| `description` | string | Descricao do produto |
| `sale_price` | integer | Preco de venda em centavos |
| `quantity` | integer | Quantidade |
| `sale_date` | timestamp | Data da venda |
| `payment_method` | string | Forma de pagamento: `CASH`, `PIX`, `DEBIT` ou `CREDIT` |
| `terminal_id` | string? | Maquininha usada (nulo em dinheiro) |
| `terminal_name` | string? | Copia do nome da maquininha |
| `brand` | string? | Bandeira (debito/credito) |
| `installments` | integer? | Parcelas (credito) |
| `tariff_rate_bps` | integer | Tarifa aplicada em basis points (2,49% = `249`); `0` sem tarifa |
| `tariff_id` | string? | Tarifa usada; nulo sem tarifa |
| `tariff_amount_cents` | integer | Valor da taxa em centavos; `0` sem tarifa |

Os campos de pagamento e tarifa sao um snapshot imutavel: alterar a tarifa depois nao reescreve vendas antigas. Sem tarifa vigente na data da venda, ela e salva sem taxa.

### Maquininhas: `stores/{storeId}/paymentTerminals/{terminalId}`

| Campo | Tipo | Descricao |
|---|---|---|
| `name` | string | Nome da maquininha |
| `active` | boolean | `false` arquiva (nunca apagar se houver tarifas ou vendas) |
| `enabledMethods` | list | `CASH`, `PIX`, `DEBIT`, `CREDIT` habilitados |
| `brands` | list | Bandeiras desta maquininha (nao ha catalogo global) |

### Tarifas: `.../paymentTerminals/{terminalId}/tariffs/{tariffId}`

Somente criacao e leitura (historico imutavel). Alterar uma tarifa e inserir um novo registro com nova vigencia.

| Campo | Tipo | Descricao |
|---|---|---|
| `method` | string | `CASH`, `PIX`, `DEBIT` ou `CREDIT` |
| `brandId` | string? | Bandeira (debito/credito) |
| `installments` | integer? | Numero exato da parcela (credito) |
| `rateBps` | integer | Tarifa em basis points |
| `effectiveFrom` | date | Inicio da vigencia |
| `createdAt` | timestamp | Criacao |

Vale a tarifa de maior `effectiveFrom` menor ou igual a data da venda para a mesma maquininha, tipo, bandeira e parcela. Detalhes em `docs/tarifas.md` e `docs/planejamento-pagamento-venda.md`.

Os valores monetarios persistidos sao inteiros em centavos e as tarifas, inteiros em basis points; a interface converte-os para `BigDecimal`. A senha e gerenciada pelo Firebase Authentication e nao e armazenada no Firestore.

As regras em `firestore.rules` restringem o acesso aos dados da loja a uma sessao autenticada cujo UID corresponda ao `storeId`. Lotes de compra (estoque/FIFO) e relatorios ainda nao possuem colecoes implementadas.


## Atualizacao reativa das telas

Ao cadastrar uma compra/venda, as demais telas (como o Home) se atualizam sozinhas, sem `ActivityResult`, `onResume()` ou recarga manual:

1. A tela de cadastro grava o documento no Firestore por meio de um caso de uso.
2. O `addSnapshotListener` (em `FirebasePurchaseFirestoreClient`) detecta a mudanca e emite os dados novos em um `Flow`.
3. O Repository repassa o `Flow` e a ViewModel (por exemplo, `HomeViewModel`) atualiza seu `StateFlow`.
4. O Compose observa o estado e recompoe a interface.

O resumo mensal e as ultimas vendas sao observados por `observeMonthlySummary` e `observeLatestPurchases`. Cada ViewModel observa apenas os dados de que a sua tela precisa, e o listener e removido via `awaitClose` quando o fluxo deixa de ser coletado. O Firestore pode emitir primeiro dados do cache local, antes da confirmacao do servidor. Detalhes em `docs/atualizacao_reativa_lucro_varejo.md`.

### Vínculo da tela de Vendas

O vínculo real entre o Firestore e a tela de Vendas está em
`FirebaseSalesRemoteDataSource.observeSalesChanges`: o `addSnapshotListener` observa a
coleção `stores/{storeKey}/purchases` e envia uma emissão pelo `callbackFlow` a cada mudança.
O `ObserveSalesChangesUseCase` não se conecta diretamente ao Firebase; ele apenas repassa esse
`Flow` do Repository para o `SalesViewModel`, que recarrega a lista preservando os filtros ativos.

```text
Firestore purchases -> addSnapshotListener -> callbackFlow -> Repository
-> ObserveSalesChangesUseCase -> SalesViewModel -> SalesScreen
```

O listener de produtos segue o mesmo padrão, mas observa `stores/{storeKey}/products`. Criar um
produto não altera Home ou Vendas; essas telas reagem apenas quando uma venda é gravada em
`purchases`.