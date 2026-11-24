# Instruções do Copilot para o Lucro Varejo

## Fontes e precedência
Documentos do projeto, em ordem de precedência quando divergirem:
1. `INSTRUCTION.md`: regras gerais, padrões técnicos e metas do produto.
2. `docs/planejamento-pagamento-venda.md`: forma de pagamento e tarifa na venda (sobrescreve `docs/tarifas.md` na integração com a venda, que lá aparece como "fora do escopo").
3. `docs/tarifas.md`: cadastro de maquininhas, bandeiras e tarifas.
4. `docs/atualizacao_reativa_lucro_varejo.md`: contexto e justificativa da atualização reativa.
5. `README.md`: estrutura atual do banco e do fluxo reativo.

Em conflito entre código e documentação, o código mostra o estado atual e o `INSTRUCTION.md` a meta. Não "corrija" o código para a meta sem pedido explícito.

## Visão geral
- App Android nativo (Kotlin, Jetpack Compose, Material 3, Hilt, Firebase Auth, Cloud Firestore). Código em `app/src/main/java/com/cabral/lucrovarejo`. Textos da UI em português.
- Indicador principal: lucro bruto = receita líquida − CMV, com o custo real dos lotes vendidos (FIFO), nunca o custo atual do catálogo. Não chamar de "lucro líquido".
- **Terminologia (armadilha):** no `INSTRUCTION.md`, "compra" é o lote de estoque (`PurchaseLot`) e "venda" é `Sale`. No código atual, `PurchaseModel`/`purchases`/`AddPurchaseScreen` representam a **venda** (têm `sale_price`, `sale_date`, forma de pagamento e tarifa). Os lotes e vendas com vários itens/FIFO ainda não existem. Não renomeie coleções ou classes sem pedido explícito.

## Conta e acesso
- Uma conta por loja (usuário + senha), compartilhada pelos funcionários. Sem login com Google.
- Firebase Auth e-mail/senha com o nome de usuário normalizado convertido em e-mail técnico interno; mesma normalização no cadastro e no login; mensagens de erro sem revelar se a conta existe.
- Nunca salvar senha em Firestore, preferências, logs ou código. Sem segredos no app. Composables não chamam Firebase diretamente.

## Dados e dinheiro
- Firestore por loja (`storeId` = UID autenticado):
  - `stores/{storeId}/products/{id}`
  - `stores/{storeId}/purchases/{id}` (vendas, ver terminologia)
  - `stores/{storeId}/paymentTerminals/{id}` e `.../tariffs/{id}`
  - Regras em `firestore.rules`; atualizar ao adicionar campos ou coleções.
- Nunca usar `Float`/`Double` para dinheiro. UI/app usa `BigDecimal`; request/persistência usa inteiro (centavos em `Long`, tarifas em basis points `Int`). Models separados para UI e request, com conversões explícitas e sem arredondamento silencioso (rejeitar fração menor que 1 centavo ou overflow).
- Histórico imutável: alterar catálogo ou tarifa não reescreve vendas, lotes ou relatórios. Impedir venda acima do estoque.
- Meses agrupados pela data da venda no fuso local. Compra ainda em estoque não entra no CMV.
- Taxa da maquininha: é só salva e exibida na venda; **não altera o lucro bruto** até a regra ser decidida (em aberto no planejamento).

## Maquininhas e tarifas
- `PaymentMethods` = `CASH, PIX, DEBIT, CREDIT` (não existe mais `CARD`; não tratar `"CARD"` legado).
- Dinheiro: tarifa opcional ("Cobra tarifa?"). Pix: tarifa única. Débito: por bandeira. Crédito: por bandeira + parcela individual (sem faixas).
- Bandeiras cadastradas dentro de cada maquininha (sem catálogo global), exibidas só por nome.
- Tarifa em basis points inteiros (2,49% = 249); UI em `BigDecimal`; validar 0–100% e até 2 casas.
- Tarifas são append-only com `effectiveFrom` (data sem hora): vale a de maior `effectiveFrom <= data` para maquininha + tipo + bandeira + parcela (`GetFilteredCurrentTariffUseCase`). Mesma chave + mesma data é rejeitada. Sem update/delete.
- Desativar maquininha = `active=false`; nunca apagar se houver tarifas ou vendas.
- Cadastro da maquininha: `SecondaryTabRow`/`Tab` só navega entre tipos; `AppSwitch` na seção habilita o método; Salvar fixo na `bottomBar` do `Scaffold` (`ui/components/IconButton.kt`); parcelas de crédito via Spinner; chip de bandeira rola até o card de tarifa ao ser marcado.
- Tour guiado ("Dicas" em Configurações ou prompt após login manual): `ui/components/CoachMark.kt` (`Modifier.coachTarget` + `CoachMarkOverlay`) destaca elementos reais de `MachineSaleScreen`; o passo é derivado do estado do formulário. Nome da loja é editável em Configurações via `StoreViewModel.updateDisplayName`; `firestore.rules` valida `displayName` e precisa ser publicada.

## Pagamento e tarifa na venda (`AddPurchaseScreen`)
- Spinners em cascata: forma de pagamento → maquininha → bandeira → parcelas (Dinheiro: nenhum; Pix: maquininha; Débito: maquininha + bandeira; Crédito: os três). Cada um só habilita após o anterior; trocar um limpa os seguintes.
- Maquininhas: só `active` com o método em `enabledMethods`. Bandeiras e parcelas: só as que têm tarifa cadastrada.
- Trocar a data da venda recalcula a tarifa e revalida a seleção (avisar e limpar o que não tiver tarifa vigente).
- Sem tarifa vigente **não bloqueia**: salva com `tariffRateBps = 0`, `tariffAmountCents = 0`, `tariffId = null`. Dinheiro usa a tarifa `CASH` do banco (provavelmente 0).
- Snapshot imutável salvo na venda: `paymentMethod`, `terminalId`, `terminalName`, `brand`, `installments`, `tariffRateBps`, `tariffId`, `tariffAmountCents`. Taxa = `valorVendaCents * rateBps / 10_000` com arredondamento explícito e testado (HALF_UP). Registros antigos usam defaults (`null`/0).
- Edição de venda mantém o snapshot, salvo se o usuário mudar forma de pagamento, maquininha, bandeira, parcela ou data.
- Tarifas são carregadas por ViewModel (terminais ativos ao abrir; tarifas ao escolher a maquininha), sem cache global; a resolução por data é função pura local.
- Em aberto: dinheiro com várias maquininhas com tarifa de dinheiro (sugestão: a vigente mais recente); exibir taxa em Vendas/Home.

## Arquitetura e padrões de UI
- Camadas: apresentação, ViewModel/estado, casos de uso (regra relevante), repositório/datasource. Cálculo de lucro, FIFO e tarifas testáveis sem Android/Firebase. Evoluir gradualmente para os módulos `app`, `core/common`, `core/data`, `features/loggedout|loggedin`, sem telas conhecerem Firebase.
- Compose: separar composable stateful (ViewModel, estado, navegação) do stateless (estado + callbacks, usado em previews).
- Textos em `res/values/strings.xml` com `stringResource` (ViewModels podem guardar `@StringRes`); medidas em `dimens.xml` com `dimensionResource`.
- Reutilizar tema e componentes; estilização repetida de Material3 vai em wrapper em `ui/components` (ex.: `AppSwitch` com `appSwitchColors()`).
- Telas dentro do `Scaffold` de `LoggedFlow` (que repassa `innerPadding`) não aplicam `windowInsetsPadding(WindowInsets.safeDrawing)`; só telas fora dele (Login, Cadastro).
- `LoggedFlow`: o `Scaffold` usa `containerColor = surface` (com uma faixa verde só atrás dos cantos arredondados) e fica sem `bottomBar`. O `BottomNavBar` é overlay (fade) visível só nas rotas de aba (Home, Vendas, Produtos, Config). Sua altura é medida (`onSizeChanged`) e descontada do inset da barra de navegação para virar `tabBottomPadding`, aplicado por rota de aba, para o conteúdo não mudar de tamanho durante a animação (evita o "layout descendo" e o flash verde). O teclado é tratado com `consumeWindowInsets(innerPadding).imePadding()` no conteúdo. Não voltar a usar `bottomBar` do `Scaffold` para a barra.
- Datas de vigência/início vêm pré-preenchidas com a data atual.
- Rolar até elemento recém-exibido: `BringIntoViewRequester` + `bringIntoView()` dentro de `LaunchedEffect` chaveado no estado, nunca direto no clique.
- Telas de edição não usam `SavedStateHandle` injetado pelo Hilt: o model vai no `savedStateHandle` do `LoggedNavigator`, é lido pelo `NavHost`, passado como parâmetro e carregado com `loadX(model)` em `LaunchedEffect`.
- Estados vazio, carregamento, erro, validação e confirmação antes de excluir/cancelar. Acessibilidade básica.
- Mudanças pequenas, sem afetar telas, rotas ou build fora do escopo.

## Atualização reativa
- Firestore é a fonte de verdade. O datasource expõe `Flow` via `addSnapshotListener` + `callbackFlow` com `awaitClose`; o Repository repassa; o use case só repassa (sem conhecer Firebase); uma ViewModel por tela converte em `StateFlow` (ex.: `stateIn` com `WhileSubscribed(5_000)`) e recalcula derivados a cada emissão.
- Não usar `ActivityResult`, `onResume()`, flags em `savedStateHandle` nem refresh manual para sincronizar telas após salvar. `ActivityResult` só serve para devolver resultado de navegação.
- Cada tela observa só o que precisa; compartilhar fluxos apenas se houver listeners ou consultas redundantes. O Firestore pode emitir cache local antes do servidor.
- Vendas: `FirebaseSalesRemoteDataSource.observeSalesChanges` observa `stores/{storeKey}/purchases`; Home e Vendas reagem a `purchases`, produtos observam `products` (criar produto não altera Home/Vendas).
- Testes de ViewModel/Repository usam fluxos falsos (`MutableStateFlow`) e verificam atualização por nova emissão.
