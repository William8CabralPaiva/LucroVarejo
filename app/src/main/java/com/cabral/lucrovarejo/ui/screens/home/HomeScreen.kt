package com.cabral.lucrovarejo.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import com.cabral.lucrovarejo.ui.theme.DestructiveRed
import com.cabral.lucrovarejo.ui.components.CENTS_PER_UNIT
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeScreen(
    goToSalesScreen: () -> Unit,
    onEditPurchase: (PurchaseModel) -> Unit = {},
    viewModel: HomeViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()
    val latestPurchases = remember(state.latestPurchases) {
        state.latestPurchases.toImmutableList()
    }

    HomeContent(
        state = state,
        latestPurchases = latestPurchases,
        isLatestSalesLoading = state.isLatestPurchasesLoading,
        hasLatestSalesError = state.hasLatestPurchasesError,
        onMonthYearChange = resolvedViewModel::onMonthYearChanged,
        goToSalesScreen = goToSalesScreen,
        onEditPurchase = onEditPurchase,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onMonthYearChange: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
    latestPurchases: ImmutableList<PurchaseModel> = persistentListOf(),
    isLatestSalesLoading: Boolean = false,
    hasLatestSalesError: Boolean = false,
    goToSalesScreen: () -> Unit = {},
    onEditPurchase: (PurchaseModel) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.spacing_16dp)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16dp))
        ) {
            // A borda agrupa o que muda com o mês; as últimas vendas ficam fora, sem filtro.
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(dimensionResource(R.dimen.spacing_16dp)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16dp))
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

                    if (state.isLoading && state.summary == MonthlyPurchaseSummary()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else if (state.errorMessage == null) {
                        // Mantém o resumo anterior durante a troca de mês e faz a transição dos valores.
                        Crossfade(
                            targetState = state.summary,
                            animationSpec = tween(durationMillis = 300),
                            label = "monthly-summary"
                        ) { summary ->
                            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16dp))) {
                                MonthlySummaryContent(summary)
                            }
                        }
                        if (state.isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            LatestSalesSection(
                purchases = latestPurchases,
                isLoading = isLatestSalesLoading,
                hasError = hasLatestSalesError,
                onShowAll = goToSalesScreen,
                onEditPurchase = onEditPurchase,
            )
        }
    }
}

@Composable
private fun MonthlySummaryContent(summary: MonthlyPurchaseSummary) {
    AlertPurchase(
        price = summary.totalSales.toCurrencyAmount(),
        productsSize = summary.productsSold.toInt(),
        modifier = Modifier.fillMaxWidth()
    )
    if (summary.totalTariff > 0) {
        AlertPurchase(
            price = summary.totalTariff.toCurrencyAmount(),
            productsSize = 0,
            titleRes = R.string.alert_tariff_title,
            icon = Icons.Outlined.PointOfSale,
            containerColor = DestructiveRed,
            contentColor = Color.White,
            showProductsInfo = false,
            showTrendIcon = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16dp))
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

@Composable
private fun LatestSalesSection(
    purchases: ImmutableList<PurchaseModel>,
    isLoading: Boolean,
    hasError: Boolean,
    onShowAll: () -> Unit,
    onEditPurchase: (PurchaseModel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8dp))) {
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
                    onClick = { onEditPurchase(purchase) },
                )
            }
        }
    }
}

private fun Long.toCurrencyAmount(): Double = toDouble() / CENTS_PER_UNIT

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
                    totalTariff = 12_500,
                    productsSold = 32
                ),
                isLoading = false
            ),
            onMonthYearChange = { _, _ -> },
            goToSalesScreen = {},
        )
    }
}
