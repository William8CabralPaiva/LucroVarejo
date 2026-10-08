# Instrucoes do projeto Lucro Varejo

## Objetivo

O Lucro Varejo e um aplicativo Android para um pequeno varejista registrar compras e vendas, acompanhar o estoque e entender quanto lucrou em cada mes. O indicador principal deve ser o lucro bruto das vendas, calculado com o custo real dos produtos vendidos, e nao com o custo atual cadastrado no produto.

Use como referencia de organizacao e estilo o projeto [Lucro de Receitas](https://github.com/William8CabralPaiva/LucrodeReceitas/tree/main-compose), especialmente sua separacao entre `app`, `core` e funcionalidades em `features`, agrupadas entre fluxos autenticados e nao autenticados. Adapte essa organizacao ao escopo de varejo; nao copie funcionalidades de receitas que nao se aplicam.

## Requisitos funcionais

### Conta e acesso

- O proprietario cria uma conta para o estabelecimento com nome de usuario e senha.
- O proprietario e os demais funcionarios usam a mesma conta e os mesmos dados da loja. Nao criar um usuario individual para cada funcionario nesta primeira versao.
- O login deve solicitar nome de usuario e senha. Nao oferecer login com Google.
- Usar Firebase Authentication com o provedor de e-mail e senha. Como esse provedor nao aceita nome de usuario nativamente, converter o nome de usuario normalizado em um identificador interno estavel (por exemplo, um e-mail tecnico reservado para o app); esse identificador nunca deve ser apresentado como requisito para o usuario.
- Aplicar a mesma normalizacao no cadastro e no login, tratar nomes de usuario duplicados e apresentar mensagens de erro claras sem revelar se uma conta existe.
- Nao salvar senha no Firestore, preferencias locais, logs ou codigo. Aplicar regras do Firestore para que os dados da loja so possam ser acessados por uma sessao autenticada autorizada.
- Manter cadastro, login, sessao e saida implementados atraves de uma camada de autenticacao, sem acoplar chamadas do Firebase diretamente aos composables.

### Produtos, compras e estoque

- Manter um catalogo de produtos com identificador estavel, nome e dados atuais para facilitar novos lancamentos, como preco sugerido de venda.
- Registrar cada compra como um lote independente com produto, data, quantidade e custo unitario efetivamente pago. O custo total da compra pode incluir descontos e custos adicionais informados pelo usuario.
- Uma alteracao posterior no catalogo ou no preco de compra atual nao pode alterar lotes, vendas ou relatorios historicos.
- Controlar saldo de estoque a partir das quantidades compradas e vendidas. Impedir vendas acima do saldo disponivel e validar quantidades e valores positivos.
- Quando o mesmo produto for comprado por precos diferentes, preservar o custo por lote. Para a primeira versao, consumir os lotes pelo criterio FIFO (primeiro que entrou, primeiro que sai), registrando em cada item de venda a quantidade e o custo unitario do lote consumido. Nao calcular o custo da venda usando o preco de compra atual do catalogo.

### Vendas e lucro

- Registrar cada venda com data, produto, quantidade, preco unitario efetivamente vendido e eventual desconto.
- Salvar na venda uma copia imutavel do preco de venda aplicado e do custo unitario dos lotes consumidos. Alteracoes futuras no cadastro nao podem reescrever esses valores.
- Permitir mais de um item por venda e calcular os valores por item antes de agregar o total da venda.
- Calculos de referencia:
  - Receita liquida do item = (quantidade x preco unitario vendido) - desconto.
  - Custo dos produtos vendidos (CMV) = soma de (quantidade consumida de cada lote x custo unitario daquele lote).
  - Lucro bruto do item = receita liquida do item - CMV do item.
  - Lucro bruto do mes = soma da receita liquida das vendas do mes - soma do CMV dos itens vendidos no mes.
- Agrupar o resultado mensal pela data da venda, usando o fuso horario local. Uma compra so compoe o CMV quando seu produto e vendido; compra ainda em estoque nao deve ser contabilizada como prejuizo do mes.
- Exibir receita liquida, CMV e lucro bruto em separado para que o numero seja compreensivel. Nao chamar esse resultado de lucro liquido: despesas operacionais, impostos e taxas ficam fora do escopo inicial ate existir um modulo proprio para registra-los.
- Prever estados vazios, carregamento, falhas de conexao, validacao de formularios e confirmacao antes de excluir ou cancelar lancamentos. Uma correcao nao pode apagar silenciosamente o historico financeiro.

## Modelo de dominio sugerido

- `Store`: identificador, nome e configuracoes da loja.
- `Product`: identificador, nome, unidade, status e preco de venda sugerido atual.
- `PurchaseLot`: identificador, produto, data, quantidade comprada, quantidade restante e custo unitario pago.
- `Sale`: identificador, data, itens e totais calculados.
- `SaleItem`: produto, quantidade, preco unitario vendido, desconto e alocacoes de custo por lote.

Os nomes podem seguir as convencoes Kotlin do repositorio. Use tipos monetarios exatos (por exemplo, centavos em `Long` ou `BigDecimal` na camada de dominio); nao use `Float` ou `Double` para calcular dinheiro. Persistir datas com formato consistente e converter para o fuso local apenas para apresentacao e agrupamento mensal.

## Organizacao e padroes tecnicos

- O app atual e um projeto Android nativo em Kotlin com Jetpack Compose e Material 3. Preserve essa stack e as convencoes ja usadas em `app/src/main/java/com/cabral/lucrovarejo`.
- Ao estruturar novas funcionalidades, seguir gradualmente o padrao modular do projeto de referencia: `app` para composicao e navegacao; `core/common` para componentes e tipos compartilhados; `core/data` para acesso a dados; e modulos em `features/loggedout` e `features/loggedin` para os fluxos correspondentes. Incluir os modulos no Gradle e manter dependencias apontando para dentro, sem fazer telas conhecerem diretamente Firebase.
- Organizar cada funcionalidade em apresentacao, estado/ViewModel, casos de uso quando houver regra de negocio relevante e repositorio/fontes de dados. A regra de calculo de lucro e consumo de lotes deve ser testavel sem Android ou Firebase.
- Em telas com Jetpack Compose, separar composables stateful e stateless: o stateful conecta ViewModel, coleta o estado da tela e encaminha eventos/efeitos de navegacao; o stateless recebe o estado e callbacks por parametros, sem acessar ViewModel ou depender de navegacao. Usar o stateless em previews e facilitar sua reutilizacao.
- Manter textos apresentados ao usuario em `app/src/main/res/values/strings.xml` e acessa-los com `stringResource`; nao escrever textos da interface diretamente em composables ou ViewModels. ViewModels podem armazenar IDs de recursos anotados com `@StringRes`, sem depender de `Context`.
- Manter medidas de layout em `app/src/main/res/values/dimens.xml` e acessa-las com `dimensionResource`; evitar valores `dp` diretamente nos composables de tela.
- Reutilizar o tema, componentes, navegacao e padroes de estado existentes antes de criar alternativas. Manter textos da interface em portugues e seguir acessibilidade basica (rotulos, contraste e alvos de toque).
- Colocar configuracao Firebase nos arquivos e mecanismos apropriados do projeto. Nunca incluir chaves privadas, credenciais de servidor ou segredos no app.
- Evitar alterar telas e comportamentos fora do escopo da funcionalidade em andamento. Fazer mudancas pequenas que preservem o build e as rotas existentes.

## Telas previstas

1. **Acesso**: splash, login com usuario e senha e cadastro da conta da loja.
2. **Inicio**: resumo do mes selecionado com receita, CMV, lucro bruto e atalhos para registrar compra ou venda.
3. **Produtos**: lista, busca, cadastro e edicao de produtos; exibir saldo atual e preco sugerido.
4. **Compras**: historico e cadastro de compras/lotes, com data, quantidades e custo unitario.
5. **Vendas**: historico e cadastro de vendas; permitir selecionar produtos e quantidades sem ultrapassar o estoque disponivel.
6. **Resumo**: selecao de mes, totais e detalhamento das vendas/lucro por produto.
7. **Configuracoes**: dados da loja, preferencias e encerramento da sessao.

## Sequencia de implementacao

1. **Base**: definir configuracao Firebase e modelo de conta da loja; implementar cadastro, login por usuario/senha e persistencia da sessao.
2. **Catalogo e compras**: estruturar os modulos necessarios, persistir produtos e lotes e apresentar o estoque derivado das compras.
3. **Vendas e CMV**: implementar validacoes, alocacao FIFO, snapshots historicos e calculos monetarios testados.
4. **Resumo mensal**: agrupar vendas pelo mes local e apresentar receita liquida, CMV e lucro bruto.
5. **Acabamento**: tratar estados de erro/vazio/carregamento, acessibilidade, regras de seguranca do Firestore e testes integrados das operacoes relevantes.

## Criterios de aceite

- Um proprietario consegue criar a conta da loja e entrar usando somente usuario e senha; nao existe fluxo de Google.
- Funcionarios que entrarem com as mesmas credenciais veem os mesmos produtos, compras, vendas e resumos da loja.
- Atualizar o custo ou o preco sugerido de um produto nao modifica compras nem vendas anteriores.
- Compras do mesmo produto com custos diferentes mantem seus lotes e o custo de cada venda segue as alocacoes FIFO.
- Nao e possivel vender quantidade superior ao estoque disponivel.
- O resumo de cada mes confere com a soma das receitas liquidas menos o CMV das vendas daquele mes, inclusive quando os custos de compra variam entre lotes.
- Regras de calculo, arredondamento, validacao de saldo e agrupamento mensal possuem testes unitarios.
- Dados financeiros nao podem ser lidos ou alterados por sessoes sem autorizacao nas regras do Firebase.
