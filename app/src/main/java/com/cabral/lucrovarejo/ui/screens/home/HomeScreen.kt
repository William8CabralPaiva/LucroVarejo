package com.cabral.lucrovarejo.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.ui.components.AlertPrice
import com.cabral.lucrovarejo.ui.components.AlertPriceState
import com.cabral.lucrovarejo.ui.components.AlertPurchase
import com.cabral.lucrovarejo.ui.components.EmptyList
import com.cabral.lucrovarejo.ui.components.LastSaleItem
import com.cabral.lucrovarejo.ui.components.MonthYearSelector
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun HomeScreen(
    goToProductScreen: () -> Unit,
    goToSalesScreen: () -> Unit,
    viewModel: HomeViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()

    HomeContent(
        state = state,
        latestPurchases = state.latestPurchases,
        isLatestSalesLoading = state.isLatestPurchasesLoading,
        hasLatestSalesError = state.hasLatestPurchasesError,
        onMonthYearChange = resolvedViewModel::onMonthYearChanged,
        goToProductScreen = goToProductScreen,
        goToSalesScreen = goToSalesScreen,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    latestPurchases: List<PurchaseModel> = emptyList(),
    isLatestSalesLoading: Boolean = false,
    hasLatestSalesError: Boolean = false,
    onMonthYearChange: (year: Int, month: Int) -> Unit,
    goToProductScreen: () -> Unit,
    goToSalesScreen: () -> Unit = {},
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = goToProductScreen,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add)
                )
            }
        }
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonthYearSelector(
                year = state.year,
                month = state.month,
                onMonthYearChange = onMonthYearChange
            )

            state.errorMessage?.let { error ->
                Text(
                    text = stringResource(error),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (state.errorMessage == null) {
                val summary = state.summary
                AlertPurchase(
                    price = summary.totalSales.toCurrencyAmount(),
                    productsSize = summary.productsSold.toInt(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AlertPrice(
                        price = summary.totalCost.toCurrencyAmount(),
                        state = AlertPriceState.PURCHASE,
                        modifier = Modifier.weight(1f)
                    )
                    AlertPrice(
                        price = summary.totalProfit.toCurrencyAmount(),
                        state = AlertPriceState.PROFIT,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            LatestSalesSection(
                purchases = latestPurchases,
                isLoading = isLatestSalesLoading,
                hasError = hasLatestSalesError,
                onShowAll = goToSalesScreen,
            )
        }
    }
}

@Composable
private fun LatestSalesSection(
    purchases: List<PurchaseModel>,
    isLoading: Boolean,
    hasError: Boolean,
    onShowAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.last_sales_title),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onShowAll) {
                Text(text = stringResource(R.string.last_sales_see_all))
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null
                )
            }
        }

        when {
            hasError && purchases.isEmpty() -> Text(
                text = stringResource(R.string.last_sales_load_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            isLoading && purchases.isEmpty() -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            purchases.isEmpty() -> EmptyList(onAddPurchase = onShowAll)
            else -> purchases.forEach { purchase ->
                LastSaleItem(
                    productName = purchase.name,
                    priceInCents = purchase.salePrice.toLong(),
                    saleDate = purchase.saleDate,
                )
            }
        }
    }
}

private fun Long.toCurrencyAmount(): Double = toDouble() / 100.0

@Preview(showBackground = true, name = "Home - resumo mensal")
@Composable
private fun HomeContentPreview() {
    LucroVarejoTheme {
        HomeContent(
            state = HomeUiState(
                year = 2026,
                month = 10,
                summary = MonthlyPurchaseSummary(
                    totalSales = 845_000,
                    totalCost = 520_000,
                    totalProfit = 325_000,
                    productsSold = 32
                ),
                isLoading = false
            ),
            onMonthYearChange = { _, _ -> },
            goToProductScreen = {},
            goToSalesScreen = {},
        )
    }
}
