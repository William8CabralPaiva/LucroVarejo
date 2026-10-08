# Planejamento do Firebase e dos dados do Lucro Varejo

## Objetivo

Implementar autenticação e persistência para que um varejista registre produtos, compras e vendas e acompanhe o lucro bruto mensal. O custo de cada produto vendido deve refletir o que foi efetivamente pago nos lotes consumidos, não o custo atual do catálogo.

## Análise do projeto atual e da referência

- O Lucro Varejo é um app Android nativo em Kotlin, Jetpack Compose e Material 3. Atualmente, navegação e telas ficam no módulo `:app`; login e cadastro são apenas telas sem autenticação real, e o login ainda exibe a opção Google.
- A referência [LucrodeReceitas, branch `main-compose`](https://github.com/William8CabralPaiva/LucrodeReceitas/tree/main-compose) separa o app em `app`, `core` e módulos de funcionalidades, com fluxos em `features/loggedout` e `features/loggedin`. Adotar essa organização gradualmente, preservando a stack atual e sem importar funcionalidades de receitas.
- As regras Firestore encontradas na referência permitem acesso amplo e não devem ser copiadas. O Lucro Varejo precisa restringir os dados da loja à sessão autenticada correspondente.
- O arquivo `INSTRUCTION.md` já estabelece como requisitos do produto os lotes FIFO, snapshots financeiros, valores monetários exatos e relatório mensal de lucro bruto. Este planejamento detalha a implementação desses requisitos com Firebase.

## Decisões de produto

- Cada cadastro representa uma loja. Proprietário e funcionários compartilham a mesma conta e as mesmas credenciais; não haverá contas individuais para funcionários na primeira versão.
- O nome da loja será o nome de usuário. Não terá espaços e será normalizado da mesma forma no cadastro e no login. Como a tela de entrada solicita somente nome da loja e senha, o nome normalizado precisa ser globalmente único.
- O cadastro terá nome da loja, e-mail real e senha. A entrada continuará solicitando nome da loja e senha; o e-mail será usado para recuperação.
- As quantidades serão números inteiros. Dinheiro será armazenado em centavos (`Long`), sem cálculos com `Float` ou `Double`.
- O resultado mensal será lucro bruto: receita líquida das vendas menos CMV. Compras ainda em estoque, despesas operacionais, impostos e taxas não serão tratados como prejuízo ou lucro líquido nesta primeira versão.

## Autenticação Firebase

Firebase Authentication com provedor Email/Password será usado por não exigir integração com Google. Como esse provedor exige um e-mail para autenticar, gerar um alias técnico estável a partir do nome da loja normalizado. Esse alias será interno e nunca será apresentado como credencial da pessoa.

Fluxos:

1. **Cadastro:** validar e normalizar nome da loja, validar e-mail e senha, criar credencial no Firebase Auth com o alias técnico e criar o perfil da loja no Firestore.
2. **Login:** normalizar o nome informado, converter para o mesmo alias e autenticar com a senha. A interface não pede nem mostra o alias ou e-mail como nome de usuário.
3. **Sessão e saída:** observar a sessão Firebase para decidir o fluxo inicial e encerrar a sessão no logout.
4. **Recuperação:** usar o e-mail real cadastrado. Um serviço confiável no backend deve gerar o link de redefinição para a conta identificada pelo alias e enviá-lo ao endereço real. Não enviar links ou permitir a consulta do e-mail por clientes não autenticados.

O Firebase Auth e o Firestore devem ser acessados por repositórios/casos de uso, nunca diretamente pelos composables. Mensagens de falha não devem revelar se uma loja ou e-mail existe. O cadastro também precisa tratar falhas entre a criação da conta Auth e do perfil Firestore, evitando contas parcialmente utilizáveis. Senhas não podem ser gravadas no Firestore, preferências, logs ou código.

## Modelo de dados no Firestore

Todos os dados da loja ficam sob `stores/{uid}`, onde `{uid}` é o UID do Firebase Authentication:

| Entidade | Localização sugerida | Dados e comportamento |
|---|---|---|
| `Store` | `stores/{uid}` | Nome/usuário normalizado, e-mail de recuperação, fuso horário e datas de criação/atualização. |
| `Product` | `stores/{uid}/products/{productId}` | ID estável, nome, unidade, status e preço de venda sugerido atual. Não é a fonte dos valores históricos. |
| `Purchase` | `stores/{uid}/purchases/{purchaseId}` | Data e dados gerais da compra, incluindo descontos/custos adicionais informados. |
| `PurchaseLot` | `stores/{uid}/lots/{lotId}` | Produto, data de entrada, quantidade comprada, saldo restante e custo unitário efetivamente pago. Um novo preço de compra cria um novo lote. |
| `Sale` | `stores/{uid}/sales/{saleId}` | Data e itens da venda, valores totais e snapshots dos valores praticados. |
| `SaleItem` | Integrado à venda ou subcoleção | Produto, quantidade, preço unitário vendido, desconto, receita líquida e alocações do CMV por lote. |

Produtos, lotes e vendas precisam manter IDs estáveis. Alterar o catálogo ou o preço sugerido não pode alterar compras e vendas anteriores. Quando houver descontos ou custos adicionais na compra, alocá-los ao custo dos lotes de forma explícita e registrar o custo unitário final pago.

## Estoque, vendas e cálculo do lucro

- O estoque disponível deriva dos lotes: quantidades compradas menos quantidades consumidas em vendas.
- Ao vender um produto com vários lotes, consumir primeiro os lotes mais antigos (FIFO).
- A venda deve registrar quanto foi consumido de cada lote e o custo unitário daquele lote, mantendo o CMV histórico imutável.
- Validar quantidade inteira positiva, valores válidos e estoque suficiente antes de confirmar a venda.
- Atualizar venda, alocações e saldos dos lotes atomicamente, com transação Firestore ou mecanismo equivalente, para impedir estoque negativo em vendas concorrentes.
- Permitir mais de um produto por venda; calcular cada item antes de agregar os totais.

Fórmulas:

```text
receita líquida do item = (quantidade × preço unitário vendido) − desconto
CMV do item = soma(quantidade consumida de cada lote × custo unitário do lote)
lucro bruto do item = receita líquida do item − CMV do item
lucro bruto do mês = soma da receita líquida das vendas − soma do CMV dos itens vendidos
```

Agrupar pelo dia/mês da venda no fuso horário da loja. Uma compra só compõe o CMV quando os produtos forem vendidos; estoque não vendido não é custo de venda do mês.

## Regras de segurança e configuração

- Configurar o Firebase e suas dependências nos arquivos Gradle adequados ao projeto e manter a configuração de ambiente fora do controle de versão quando contiver dados locais.
- Não incluir credenciais privadas, chaves de servidor ou segredos no aplicativo Android. Configurações públicas necessárias ao Firebase não substituem regras de segurança.
- Criar regras Firestore permitindo acesso aos documentos de `stores/{uid}` e subcoleções somente quando a sessão estiver autenticada e `request.auth.uid == uid`.
- Não permitir leitura pública de perfis, e-mails ou mapeamentos de nomes de loja.
- Validar as regras com Firebase Emulator Suite/testes de integração antes de disponibilizar a persistência.
- A recuperação por e-mail exige um backend confiável, como Cloud Function usando Admin SDK, e um provedor de envio configurado. Esse serviço deve limitar abuso e evitar enumeração de contas.

## Organização de código proposta

- `:app`: inicialização Firebase, composição de dependências e navegação.
- `:core:common`: tipos compartilhados, valores monetários e contratos comuns.
- `:core:data` e camada remota: repositórios, fontes Firebase, conversores e mapeamento de erros.
- `:features:loggedout:auth`: login, cadastro, recuperação e estados de autenticação.
- `:features:loggedin:products`: catálogo de produtos.
- `:features:loggedin:purchases`: compras e lotes.
- `:features:loggedin:sales`: registro de vendas e consumo FIFO.
- `:features:loggedin:summary`: resumo mensal e detalhamento por produto.

Os módulos podem ser introduzidos por etapas, evitando uma migração grande de uma só vez. A regra de negócio deve depender de contratos e modelos de domínio, não do Firebase ou Android, para poder ser testada isoladamente.

## Etapas de implementação

1. **Base:** definir módulos Gradle incrementais, configuração Firebase, modelos de domínio e contratos de repositório.
2. **Conta da loja:** implementar cadastro, normalização e validação do nome de usuário, alias Auth, login, sessão, logout e recuperação por e-mail com backend.
3. **Catálogo e compras:** persistir produtos e lotes, calcular saldo de estoque e criar regras Firestore por UID.
4. **Vendas e CMV:** implementar validação de estoque, consumo FIFO atômico, snapshots imutáveis e cálculos em centavos.
5. **Resumo mensal:** apresentar receita líquida, CMV e lucro bruto agrupados pelo fuso local da loja.
6. **Confiabilidade:** completar estados de carregamento, erro e lista vazia, confirmação de correções/cancelamentos e testes unitários e de integração.

## Critérios de aceite

- O proprietário consegue cadastrar uma loja com nome, e-mail real e senha, e entrar somente com nome da loja e senha. Não há login Google.
- Funcionários com as mesmas credenciais acessam os mesmos dados da loja.
- Uma sessão sem autorização não consegue consultar nem alterar dados financeiros da loja.
- Recuperação de senha usa o e-mail real sem expor esse endereço ou confirmar a existência de uma conta a terceiros.
- Mudanças no produto não alteram os valores históricos de compra e venda.
- Compras com custos diferentes permanecem em lotes distintos; vendas usam os lotes em FIFO e nunca excedem o estoque.
- Os totais mensais correspondem à receita líquida menos o CMV das vendas daquele mês, mesmo que os custos de compra tenham variado.
- Testes cobrem normalização, cálculos monetários, descontos, FIFO, concorrência/limite de estoque e agrupamento mensal.

## Ponto de atenção

O alias técnico deve usar um domínio reservado e uma regra de normalização estável, definidos antes de abrir cadastros reais. A recuperação de senha também depende da escolha e configuração de um provedor de e-mail confiável no backend.
