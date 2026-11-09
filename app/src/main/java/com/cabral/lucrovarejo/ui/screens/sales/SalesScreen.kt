package com.cabral.lucrovarejo.ui.screens.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.ui.components.AppOutlinedTextField
import com.cabral.lucrovarejo.ui.components.EmptyList
import com.cabral.lucrovarejo.ui.components.MonthYearSelector
import com.cabral.lucrovarejo.ui.components.PurchaseItem
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.utils.DateUtils
import java.util.Calendar
import java.util.TimeZone
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private const val LOAD_MORE_THRESHOLD = 3

@Composable
fun SalesScreen(
    onAddPurchase: (() -> Unit)? = null,
    onAddProduct: (() -> Unit)? = null,
    onEditPurchase: ((PurchaseModel) -> Unit)? = null,
    viewModel: SalesViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()

    SalesContent(
        state = state,
        onQueryChange = resolvedViewModel::onQueryChange,
        onLoadMore = resolvedViewModel::loadNextPage,
        onRetry = resolvedViewModel::retry,
        onMonthYearChange = resolvedViewModel::onMonthYearChanged,
        onAddPurchase = onAddPurchase,
        onAddProduct = onAddProduct,
        onEditPurchase = onEditPurchase
    )
}

@Composable
fun SalesContent(
    state: SalesUiState,
    onQueryChange: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onMonthYearChange: (year: Int, month: Int) -> Unit = { _, _ -> },
    onAddPurchase: (() -> Unit)? = null,
    onAddProduct: (() -> Unit)? = null,
    onEditPurchase: ((PurchaseModel) -> Unit)? = null,
) {
    var showNoProductsDialog by remember { mutableStateOf(false) }

    val addPurchaseIfHasProducts: (() -> Unit)? = onAddPurchase?.let { add ->
        { if (state.hasProducts == false) showNoProductsDialog = true else add() }
    }

    if (showNoProductsDialog) {
        AlertDialog(
            onDismissRequest = { showNoProductsDialog = false },
            title = { Text(stringResource(R.string.sales_no_products_title)) },
            text = { Text(stringResource(R.string.sales_no_products_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showNoProductsDialog = false
                    onAddProduct?.invoke()
                }) {
                    Text(stringResource(R.string.sales_no_products_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoProductsDialog = false }) {
                    Text(stringResource(R.string.sales_no_products_dismiss))
                }
            }
        )
    }

    val listState = rememberLazyListState()
    val entries = remember(state.items) {
        buildSalesEntries(state.items).toImmutableList()
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= info.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }

    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    LaunchedEffect(shouldLoadMore, state.items.size) {
        if (shouldLoadMore) currentOnLoadMore()
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            if (addPurchaseIfHasProducts != null) {
                FloatingActionButton(
                    onClick = addPurchaseIfHasProducts,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.sales_add)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.spacing_16dp)),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.spacing_12dp)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.spacing_16dp)),
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.spacing_8dp)
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppOutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(
                        dimensionResource(R.dimen.radius_12dp)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    },
                    placeholder = {
                        Text(stringResource(R.string.sales_search_placeholder))
                    }
                )
            }

            MonthYearSelector(
                year = state.year,
                month = state.month,
                onMonthYearChange = onMonthYearChange
            )

            SalesList(
                state = state,
                entries = entries,
                listState = listState,
                onRetry = onRetry,
                onAddPurchase = addPurchaseIfHasProducts,
                onEditPurchase = onEditPurchase,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SalesList(
    state: SalesUiState,
    entries: ImmutableList<SalesEntry>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onRetry: () -> Unit,
    onAddPurchase: (() -> Unit)?,
    onEditPurchase: ((PurchaseModel) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        state.hasError && state.items.isEmpty() -> Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.sales_load_error),
                color = MaterialTheme.colorScheme.error
            )
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.sales_retry))
            }
        }

        state.items.isEmpty() && state.query.isNotBlank() -> Box(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    horizontal = dimensionResource(R.dimen.spacing_24dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(
                    R.string.sales_search_empty,
                    state.query.trim()
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        state.items.isEmpty() -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyList(onAddPurchase = onAddPurchase ?: {})
        }

        else -> LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = dimensionResource(R.dimen.sales_list_bottom_padding)),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.spacing_8dp)
            )
        ) {
            entries.forEach { entry ->
                when (entry) {
                    is SalesEntry.Header -> item(
                        key = "header-${entry.label}"
                    ) {
                        Text(
                            text = entry.label,
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.spacing_8dp)
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is SalesEntry.Sale -> item(
                        key = "sale-${entry.purchase.key ?: entry.hashCode()}"
                    ) {
                        val purchase = entry.purchase

                        PurchaseItem(
                            productName = purchase.name,
                            salePriceInCents = purchase.salePrice.toLong() * purchase.quantity,
                            costInCents = purchase.cost.toLong() * purchase.quantity,
                            tariffInCents = purchase.tariffAmountCents,
                            quantity = purchase.quantity,
                            onClick = { onEditPurchase?.invoke(purchase) },
                        )
                    }
                }
            }

            if (state.isLoadingMore) {
                item(key = "loading-more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                dimensionResource(R.dimen.spacing_16dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.hasError) {
                item(key = "load-more-error") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.sales_load_error),
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(onClick = onRetry) {
                            Text(stringResource(R.string.sales_retry))
                        }
                    }
                }
            }
        }
    }
}

// Agrupa vendas consecutivas do mesmo dia sob um cabeçalho "06 OUTUBRO 2026".
fun buildSalesEntries(
    purchases: List<PurchaseModel>,
    timeZone: TimeZone = TimeZone.getDefault(),
): List<SalesEntry> {
    val entries = mutableListOf<SalesEntry>()
    var currentLabel: String? = null

    purchases.forEach { purchase ->
        val label = DateUtils.formatDayHeader(purchase.saleDate, timeZone)

        if (label != currentLabel) {
            entries += SalesEntry.Header(label)
            currentLabel = label
        }

        entries += SalesEntry.Sale(purchase)
    }

    return entries
}

@Preview(showBackground = true, name = "Vendas")
@Composable
private fun SalesContentPreview() {
    val today = Calendar.getInstance().time

    LucroVarejoTheme {
        SalesContent(
            state = SalesUiState(
                isLoading = false,
                items = listOf(
                    PurchaseModel(
                        key = "1",
                        cod = "001",
                        name = "Bolsa Feminina",
                        cost = 5000,
                        description = "",
                        salePrice = 8990,
                        quantity = 1,
                        saleDate = today,
                        paymentMethod = PaymentMethods.PIX
                    )
                )
            ),
            onQueryChange = {},
            onLoadMore = {},
            onRetry = {},
            onAddPurchase = {}
        )
    }
}
