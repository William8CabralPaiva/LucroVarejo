# Como o Home "fica escutando" o Firestore

Explicação trecho a trecho: **qual código faz o quê** e **por que precisa ser `Flow`**.

## A ideia em uma frase

Antes, o app **perguntava** ao Firestore uma vez ("me dê as vendas") e guardava a resposta.
Agora o app **se inscreve** no Firestore ("me avise sempre que as vendas mudarem"). Uma inscrição que entrega vários valores ao longo do tempo é exatamente o que um `Flow` representa.

| | Antes | Agora |
|---|---|---|
| Firestore | `.get().await()` | `.addSnapshotListener { ... }` |
| Retorno | um valor (`List`) | vários valores ao longo do tempo (`Flow<List>`) |
| Quem atualiza a tela | refresh manual ao voltar do cadastro | o próprio Firestore, a cada mudança |

## Por que precisa ser `Flow`?

- `suspend fun`: devolve **um** resultado e acaba. Serve para "buscar uma vez".
- `Flow`: devolve **uma sequência** de resultados. Cada mudança no banco vira um novo item na sequência.

Para "ficar escutando" é preciso poder entregar um resultado **novo** quando o banco mudar. Uma função `suspend` não consegue; um `Flow` sim.

## O caminho completo

```text
Tela de cadastro salva a venda
        |
        v
Firestore grava e avisa o listener               (FirebasePurchaseFirestoreClient)
        |
        v
Flow emite a lista nova                          (snapshotFlow / callbackFlow)
        |
        v
Repository transforma (calcula lucro, mapeia)    (PurchaseRepositoryImpl)
        |
        v
UseCase repassa o Flow                           (GetMonthlyPurchaseSummaryUseCase)
        |
        v
HomeViewModel coleta e atualiza o estado         (HomeViewModel -> uiState)
        |
        v
Compose observa o estado e redesenha             (HomeScreen)
```

---

## 1. O trecho que "escuta": `addSnapshotListener` dentro de `callbackFlow`

Arquivo: `data/purchase/datasource/FirebasePurchaseFirestoreClient.kt`

```kotlin
private fun <T> Query.snapshotFlow(map: (QuerySnapshot) -> T): Flow<T> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)                       // erro do Firestore encerra o Flow com falha
            return@addSnapshotListener
        }
        if (snapshot == null) return@addSnapshotListener
        try {
            trySend(map(snapshot))             // <- ENVIA o valor novo para quem está coletando
        } catch (failure: Exception) {
            close(failure)
        }
    }
    awaitClose { registration.remove() }       // <- para de escutar quando ninguém coleta mais
}
```

O que cada parte faz:

- **`addSnapshotListener`**: é a parte do Firestore que "fica escutando". Ele chama o bloco `{ snapshot, error -> ... }` **uma vez ao se registrar** e **de novo toda vez** que os dados da consulta mudam.
- **`callbackFlow`**: ponte entre o mundo de *callbacks* (o listener do Firestore) e o mundo de `Flow`. Tudo que você enviar com `trySend` dentro dele sai como item do `Flow`.
- **`trySend(...)`**: é o que transforma "o Firestore avisou que mudou" em "o Flow emitiu um valor novo".
- **`awaitClose { registration.remove() }`**: obrigatório no `callbackFlow`. Quando a tela/ViewModel para de coletar, ele remove o listener. Sem isso o listener ficaria vivo para sempre (vazamento de memória e leituras cobradas).
- **`close(error)`**: se o Firestore der erro (sem rede, sem permissão), o Flow termina com essa exceção, e quem coleta trata.

### Onde ele é usado

```kotlin
override fun observeLatestPurchases(storeKey: String, limit: Int): Flow<List<PurchaseRecord>> =
    purchasesCollection(storeKey)
        .orderBy(FIELD_SALE_DATE, Query.Direction.DESCENDING)
        .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
        .limit(limit.toLong())
        .snapshotFlow { snapshot -> snapshot.documents.map { /* converte para PurchaseRecord */ } }
```

A consulta (ordenar por data, limitar a 4) é a mesma de antes. A diferença é que no fim usa `.snapshotFlow { }` em vez de `.get().await()`. O resumo mensal faz igual, com `whereGreaterThanOrEqualTo` / `whereLessThan` no lugar do `limit`.

---

## 2. O Repository: transforma o Flow, sem "perguntar" de novo

Arquivo: `data/purchase/PurchaseRepositoryImpl.kt`

```kotlin
override fun observeMonthlySummary(start: Date, end: Date): Flow<MonthlyPurchaseSummary> {
    require(start.before(end)) { "Summary start timestamp must be before end timestamp" }
    return remoteDataSource.observePurchasesBetween(start, end)
        .map { purchases ->                               // <- roda a CADA emissão
            purchases.fold(MonthlyPurchaseSummary()) { summary, purchase -> /* soma vendas, custo, lucro */ }
        }
        .withPurchaseFailure()
}
```

- **`.map { }`**: toda vez que o Flow de baixo emite uma lista nova, o `map` **recalcula** o resumo. É por isso que o lucro total muda sozinho quando entra uma venda nova.
- **`.withPurchaseFailure()`** (usa `.catch { }`): converte erros do Firestore em `PurchaseFailure` (rede, permissão...), que a ViewModel sabe transformar em mensagem.
- Note que a função **não é `suspend`**: ela só devolve o Flow; quem **coleta** é que o faz "rodar".

> Importante: um `Flow` é "preguiçoso". Nada é consultado no Firestore até alguém coletar. É a ViewModel que começa a coletar.

---

## 3. A ViewModel: coleta o Flow e joga no estado da tela

Arquivo: `ui/screens/home/HomeViewModel.kt`

```kotlin
private fun observeSummary() {
    selectedMonth
        .flatMapLatest { (year, month) ->                  // <- se o mês mudar, troca a escuta
            val (start, end) = monthRange(year, month)
            getMonthlySummary(start, end)                  // <- o Flow do Repository
                .onStart { _uiState.update { it.copy(isLoading = true, errorMessage = null) } }
                .onEach { summary ->                       // <- roda a CADA emissão
                    _uiState.update { it.copy(summary = summary, isLoading = false) }
                }
                .catch { failure -> /* coloca mensagem de erro no estado */ }
        }
        .launchIn(viewModelScope)                          // <- começa a coletar
}
```

- **`.launchIn(viewModelScope)`**: é o que **inicia a escuta**. Enquanto a ViewModel existir, o Flow é coletado; quando ela for destruída, o `viewModelScope` cancela tudo e o `awaitClose` remove o listener do Firestore.
- **`.onEach { }`**: a cada valor novo vindo do Firestore, atualiza o `_uiState`. É aqui que "dado novo no banco" vira "estado novo na tela".
- **`.onStart { }`**: marca `isLoading = true` antes do primeiro valor chegar.
- **`.catch { }`**: se o Flow falhar, mostra a mensagem de erro em vez de quebrar o app.
- **`flatMapLatest` + `selectedMonth`**: quando o usuário troca o mês, a escuta do mês antigo é **cancelada** e começa a do mês novo. Sem isso ficariam dois listeners ativos.

As últimas vendas funcionam igual (`observeLatestPurchases`), com limite de 4.

---

## 4. A tela: só observa o estado

Arquivo: `ui/screens/home/HomeScreen.kt`

```kotlin
val state by resolvedViewModel.uiState.collectAsState()   // <- Compose observa o StateFlow

HomeContent(
    state = state,
    latestPurchases = state.latestPurchases,
    ...
)
```

- **`collectAsState()`**: sempre que `_uiState` mudar (por causa do `onEach` acima), o Compose **recompõe** a tela com os valores novos. Não existe nenhum código de "atualizar a tela" - ela só reage ao estado.

---

## 5. O que foi **removido** (e por que não precisa mais)

Antes existia um caminho manual para avisar o Home:

```kotlin
// LoggedFlowScreen.kt (REMOVIDO)
AddPurchaseScreen(onPurchaseSaved = navigator::markSaved)   // cadastro avisava "salvei"
fun onRouteChanged(...) { ... savedStateHandle[HOME_REFRESH_REQUESTED_KEY] = true }  // flag ao voltar ao Home

// HomeScreen.kt (REMOVIDO)
LaunchedEffect(refreshRequested) { if (refreshRequested) viewModel.refreshForCurrentMonth() }
```

Tudo isso existia porque o Home só fazia `get()` uma vez e precisava ser mandado buscar de novo. Com o listener, o Firestore avisa sozinho. Por isso sumiram:

- os callbacks `onPurchaseSaved` / `onProductSaved`;
- o `markSaved()`, o `onRouteChanged()` e a flag no `savedStateHandle`;
- o `refreshForCurrentMonth()` da ViewModel;
- o `Paging` (as últimas vendas viraram uma lista simples com `limit(4)`).

A tela de cadastro agora só salva e mostra o snackbar.

---

## 6. Como o teste prova que está reagindo

Nos testes, um `MutableStateFlow` faz o papel do Firestore: mudar o valor dele simula "o banco mudou".

```kotlin
val summary = MutableStateFlow(MonthlyPurchaseSummary())   // no repositório falso

repository.summary.value = MonthlyPurchaseSummary(totalSales = 123_400)   // "o banco mudou"
advanceUntilIdle()

assertEquals(123_400L, viewModel.uiState.value.summary.totalSales)        // a tela refletiu
```

Se o resumo fosse buscado uma vez só, esse valor novo nunca chegaria ao estado.

---

## Resumo rápido

| Peça | Papel |
|---|---|
| `addSnapshotListener` | O Firestore avisa quando algo muda |
| `callbackFlow` + `trySend` | Transforma cada aviso em um item de `Flow` |
| `awaitClose { remove() }` | Para de escutar quando ninguém mais coleta |
| `Flow` (no Repository/UseCase) | Carrega "vários valores ao longo do tempo" |
| `.map` no Repository | Recalcula o lucro a cada emissão |
| `.launchIn(viewModelScope)` | Começa a escutar (e para ao destruir a ViewModel) |
| `.onEach` + `_uiState.update` | Dado novo vira estado novo |
| `collectAsState()` | Estado novo vira tela redesenhada |

Teoria geral: `docs/atualizacao_reativa_lucro_varejo.md`.
