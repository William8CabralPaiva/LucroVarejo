package com.cabral.lucrovarejo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.ui.components.AlertPrice
import com.cabral.lucrovarejo.ui.components.AlertPriceState
import com.cabral.lucrovarejo.ui.components.AlertPurchase
import com.cabral.lucrovarejo.ui.components.MonthYearSelector
import com.cabral.lucrovarejo.ui.screens.home.HomeUiState
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

//TODO VERIFICAR SE ESTA ATUALZIANDO DPS de add item
@Composable
fun HomeScreen(
    goToConfigScreen: () -> Unit,
    goToProductScreen: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel? = null
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        onMonthYearChange = resolvedViewModel::onMonthYearChanged,
        goToConfigScreen = goToConfigScreen,
        goToProductScreen = goToProductScreen,
        onLogout = onLogout
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onMonthYearChange: (year: Int, month: Int) -> Unit,
    goToConfigScreen: () -> Unit,
    goToProductScreen: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = androidx.compose.ui.res.stringResource(error),
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

        Button(
            onClick = goToConfigScreen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Configurações & Modo Escuro")
        }
        Button(
            onClick = goToProductScreen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Testando navegação")
        }
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sair")
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
            goToConfigScreen = {},
            goToProductScreen = {},
            onLogout = {}
        )
    }
}
