# Atualização reativa das telas — Lucro Varejo

## Contexto

O app tem uma tela separada para cadastrar vendas e quatro abas, cada uma com sua própria ViewModel:

- **Dash:** lucro e últimas vendas.
- **Vendas:** lista de vendas/produtos.
- **Resumo:** gráfico para comparar vendas mensalmente.
- **Configurações:** cadastro das taxas do cartão.

A ideia é evitar atualizar manualmente todas as telas após cadastrar uma venda, usando `ActivityResult` ou `onResume()`.

## Abordagem recomendada

Usar o **Firestore como fonte de verdade**, com um Repository que disponibiliza os dados como `Flow`. Para dados que precisam ser atualizados em tempo real, o Repository pode usar `addSnapshotListener`.

Fluxo geral:

1. A tela de cadastro salva uma venda por meio de um caso de uso.
2. O Firestore registra a alteração.
3. O `addSnapshotListener` detecta a mudança e emite os dados atualizados.
4. O Repository disponibiliza as mudanças por `Flow`.
5. A ViewModel coleta o fluxo e atualiza seu estado (`StateFlow`).
6. O Compose observa o estado e recompõe a interface automaticamente.

Assim, não é necessário usar `ActivityResult` ou `onResume()` apenas para atualizar os dados observados pelo listener.

## Preciso escutar em cada tela?

Cada tela precisa receber atualizações dos dados dos quais depende, mas **não é obrigatório ter um `addSnapshotListener` independente para cada tela**.

Exemplo de responsabilidades:

- **DashViewModel:** observa os dados necessários para calcular/exibir o lucro e as últimas vendas.
- **VendasViewModel:** observa a lista que a tela precisa exibir.
- **ResumoViewModel:** observa os dados necessários para agregar as vendas por mês.
- **ConfiguraçõesViewModel:** observa as taxas do cartão se elas precisarem de atualização em tempo real.

Há duas opções:

### Opção A — Um listener por consulta

Cada método do Repository observa sua própria consulta. É uma solução simples de entender, mas pode haver listeners ou consultas redundantes.

### Opção B — Compartilhar um fluxo

O Repository centraliza a observação e compartilha um fluxo entre os consumidores. Isso pode evitar listeners duplicados, desde que o compartilhamento e o ciclo de vida do fluxo sejam configurados corretamente.

**As ViewModels podem continuar separadas.** O que pode ser compartilhado é o fluxo de dados, não necessariamente a ViewModel.

## Cuidados importantes

- Cada consulta precisa observar os dados relevantes para aquela tela.
- Valores derivados — como lucro total e agregações mensais — precisam ser recalculados quando os dados de origem mudarem.
- O listener deve ser removido adequadamente quando não for mais necessário. Ao converter callbacks para `Flow`, `callbackFlow` com `awaitClose` é uma opção comum para gerenciar isso.
- O Firestore pode emitir dados do cache local antes de confirmar o estado no servidor.
- Não é necessário fazer todas as telas observarem a mesma lista completa de vendas. Cada tela pode usar consultas ou fluxos adequados à sua necessidade.

## E o `ActivityResult`?

`ActivityResult` continua sendo útil para devolver um resultado de navegação, por exemplo, indicar que o cadastro terminou com sucesso. Porém, não é necessário usá-lo como mecanismo principal de sincronização das abas quando elas já observam os dados por fluxos reativos.

## Recomendação para o projeto

1. Manter uma ViewModel por tela.
2. Centralizar o acesso ao Firestore nos Repositories.
3. Expor os dados como `Flow` e convertê-los em estado observável na ViewModel, por exemplo, com `stateIn`.
4. Usar `addSnapshotListener` apenas para os dados que precisam de atualização em tempo real.
5. Começar com uma implementação simples e só compartilhar fluxos se houver listeners ou consultas redundantes.

### Exemplo conceitual

```kotlin
class GetSalesUseCase(
    private val repository: SalesRepository
) {
    operator fun invoke(): Flow<List<Sale>> {
        return repository.observeSales()
    }
}
```

Na ViewModel, o fluxo pode ser convertido em estado:

```kotlin
val sales = getSalesUseCase()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
```

Esse exemplo pressupõe que `observeSales()` já retorna um `Flow` alimentado pelo Repository, possivelmente usando `addSnapshotListener`.

## Próximo passo

Verificar como o Repository atual busca as vendas:

- Se usa `addSnapshotListener`, pode adaptar o resultado para um `Flow`.
- Se usa `get()`, a busca ocorre quando solicitada; nesse caso, é necessário decidir como e quando recarregar os dados ou migrar para observação em tempo real.
