package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LightBlueContainer
import com.cabral.lucrovarejo.ui.theme.LightGreenContainer
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ProfitGreen

@Composable
fun AlertPrice(
    price: Double,
    state: AlertPriceState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = state.color
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = state.icon,
                contentDescription = null,
                tint = ProfitGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Column {
                Text(
                    text = stringResource(id = state.textId),
                    style = MaterialTheme.typography.labelSmall,
                    color = ProfitGreen
                )
                Text(
                    formatAlertPrice(price),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ProfitGreen
                )
            }
        }

    }
}

internal fun formatAlertPrice(price: Double): String =
    "R$ ${"%.2f".format(java.util.Locale.forLanguageTag("pt-BR"), price)}"

enum class AlertPriceState(val color: Color, val icon: ImageVector, val textId: Int) {
    PROFIT(LightGreenContainer, Icons.AutoMirrored.Filled.TrendingUp, R.string.alert_profit_title),
    PURCHASE(LightBlueContainer, Icons.Filled.MoneyOff, R.string.alert_purchase_title)
}

@Composable
@Preview(showBackground = true)
fun AlertPriceProfitPreview() {
    LucroVarejoTheme {
        AlertPrice(50.90, AlertPriceState.PROFIT)
    }
}

@Composable
@Preview(showBackground = true)
fun AlertPricePurchasePreview() {
    LucroVarejoTheme {
        AlertPrice(50.90, AlertPriceState.PURCHASE)
    }
}