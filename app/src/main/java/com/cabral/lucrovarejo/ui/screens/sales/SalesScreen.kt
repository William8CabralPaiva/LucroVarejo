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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.ui.components.PurchaseItem
import com.cabral.lucrovarejo.ui.components.formatSaleDatePtBr
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.utils.DateUtils
import java.util.Calendar
import java.util.TimeZone

private const val LOAD_MORE_THRESHOLD = 3

@Composable
fun SalesScreen(
    onAddPurchase: (() -> Unit)? = null,
    viewModel: SalesViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()

    SalesContent(
        state = state,
        onQueryChange = resolvedViewModel::onQueryChange,
        onDateSelected = resolvedViewModel::onDateSelected,
        onLoadMore = resolvedViewModel::loadNextPage,
        onRetry = resolvedViewModel::retry,
        onAddPurchase = onAddPurchase
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesContent(
    state: SalesUiState,
    onQueryChange: (String) -> Unit,
    onDateSelected: (Long?) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onAddPurchase: (() -> Unit)? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val entries = remember(state.items) { buildSalesEntries(state.items) }
    val shouldLoadMore by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= info.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore, state.items.size) {
        if (shouldLoadMore) onLoadMore()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            if (onAddPurchase != null) {
                FloatingActionButton(
                    onClick = onAddPurchase,
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                    },
                    placeholder = { Text(stringResource(R.string.sales_search_placeholder)) }
                )
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = stringResource(R.string.sales_filter_by_date),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            state.selectedDateMillis?.let { dateMillis ->
                AssistChip(
                    onClick = { onDateSelected(null) },
                    label = {
                        Text(
                            stringResource(
                                R.string.sales_date_filter_label,
                                formatSaleDatePtBr(dateMillis)
                            )
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.sales_date_filter_clear)
                        )
                    }
                )
            }

            SalesList(
                state = state,
                entries = entries,
                listState = listState,
                onRetry = onRetry,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showDatePicker) {
        val pickerState =
            rememberDatePickerState(initialSelectedDateMillis = state.selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateSelected(pickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.date_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.date_picker_cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun SalesList(
    state: SalesUiState,
    entries: List<SalesEntry>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onRetry: () -> Unit,
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

        state.items.isEmpty() -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.sales_empty),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        else -> LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            entries.forEach { entry ->
                when (entry) {
                    is SalesEntry.Header -> item(key = "header-${entry.label}") {
                        Text(
                            text = entry.label,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is SalesEntry.Sale -> item(key = "sale-${entry.purchase.id ?: entry.hashCode()}") {
                        val purchase = entry.purchase
                        PurchaseItem(
                            productName = purchase.name,
                            salePriceInCents = purchase.salePrice.toLong() * purchase.quantity,
                            costInCents = purchase.cost.toLong() * purchase.quantity,
                            quantity = purchase.quantity,
                            onClick = {}
                        )
                    }
                }
            }
            if (state.isLoadingMore) {
                item(key = "loading-more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                        id = "1",
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
            onDateSelected = {},
            onLoadMore = {},
            onRetry = {},
            onAddPurchase = {}
        )
    }
}
