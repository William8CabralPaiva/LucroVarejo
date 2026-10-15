# Lucro Varejo

Aplicativo Android para apoiar o controle de produtos, compras e estoque de um pequeno varejo. O projeto usa Kotlin, Jetpack Compose, Firebase Authentication e Cloud Firestore.

## Banco de dados

O Cloud Firestore e um banco NoSQL orientado a documentos. Os dados ficam separados por loja, usando o UID do Firebase Authentication como identificador:

```text
stores/{storeId}
  products/{productId}
  purchases/{purchaseId}
```

`storeId` corresponde ao UID da conta autenticada. `productId` e `purchaseId` sao IDs de documentos gerados pelo Firestore. Os produtos e as compras ficam em subcolecoes da loja, nao em colecoes globais.

### Documento da loja: `stores/{storeId}`

| Campo | Tipo | Descricao |
|---|---|---|
| `storeName` | string | Nome de usuario normalizado da loja |
| `email` | string | E-mail de contato informado no cadastro |
| `createdAt` | timestamp | Data de criacao, definida pelo servidor |

### Documento de produto: `stores/{storeId}/products/{productId}`

| Campo | Tipo | Descricao |
|---|---|---|
| `cod` | string | Codigo do produto |
| `name` | string | Nome do produto |
| `cost` | integer | Custo em centavos (por exemplo, `1234` representa R$ 12,34) |
| `description` | string | Descricao do produto |

### Documento de compra: `stores/{storeId}/purchases/{purchaseId}`

| Campo | Tipo | Descricao |
|---|---|---|
| `cod` | string | Codigo do produto comprado |
| `name` | string | Nome do produto no momento da compra |
| `cost` | integer | Custo unitario em centavos |
| `description` | string | Descricao do produto |
| `sale_price` | integer | Preco de venda sugerido em centavos |
| `quantity` | integer | Quantidade comprada |
| `sale_date` | timestamp | Data da compra |
| `payment_method` | string | Forma de pagamento: `CASH`, `PIX` ou `CARD` |

Os valores monetarios persistidos sao inteiros em centavos; a interface converte-os para valores monetarios. A senha e gerenciada pelo Firebase Authentication e nao e armazenada no Firestore.

As regras em `firestore.rules` restringem o acesso aos dados da loja a uma sessao autenticada cujo UID corresponda ao `storeId`. A estrutura acima descreve o que o app persiste atualmente; vendas e relatorios ainda nao possuem colecoes implementadas.
