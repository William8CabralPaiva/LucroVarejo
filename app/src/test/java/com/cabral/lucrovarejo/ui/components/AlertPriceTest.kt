package com.cabral.lucrovarejo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.MoneyOff
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LightBlueContainer
import com.cabral.lucrovarejo.ui.theme.LightGreenContainer
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertPriceTest {
    @Test
    fun formatsPriceUsingBrazilianCurrencyDecimalSeparator() {
        assertEquals("R$ 50,90", formatAlertPrice(50.90))
    }

    @Test
    fun formatsWholePriceWithTwoDecimalPlaces() {
        assertEquals("R$ 50,00", formatAlertPrice(50.0))
    }

    @Test
    fun profitStateUsesProfitTitleColorAndIcon() {
        val state = AlertPriceState.PROFIT

        assertEquals(R.string.alert_profit_title, state.textId)
        assertEquals(LightGreenContainer, state.color)
        assertEquals(Icons.AutoMirrored.Filled.TrendingUp, state.icon)
    }

    @Test
    fun purchaseStateUsesPurchaseTitleColorAndIcon() {
        val state = AlertPriceState.PURCHASE

        assertEquals(R.string.alert_purchase_title, state.textId)
        assertEquals(LightBlueContainer, state.color)
        assertEquals(Icons.Filled.MoneyOff, state.icon)
    }
}
