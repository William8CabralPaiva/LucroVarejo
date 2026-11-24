package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LightBlueContainer
import com.cabral.lucrovarejo.ui.theme.LightGreenContainer
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ProfitGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AlertPrice(
    price: Double,
    state: AlertPriceState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_8dp)),
        color = state.color
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) {
            Icon(
                imageVector = state.icon,
                contentDescription = null,
                tint = ProfitGreen,
                modifier = Modifier.size(dimensionResource(R.dimen.size_24dp))
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_4dp)))
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
                if (price == 0.0) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_16dp)))
                    Text(
                        text = stringResource(R.string.alert_no_products),
                        style = MaterialTheme.typography.labelSmall,
                        color = ProfitGreen
                    )
                }
            }
        }

    }
}

internal const val CENTS_PER_UNIT = 100.0
private const val PREVIEW_PRICE = 50.90

internal fun formatAlertPrice(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
        isGroupingUsed = true
    }
    return "R$ ${formatter.format(price)}"
}

enum class AlertPriceState(
    val color: Color,
    val icon: ImageVector,
    val textId: Int
) {
    PROFIT(LightGreenContainer, Icons.AutoMirrored.Filled.TrendingUp, R.string.alert_profit_title),
    PURCHASE(LightBlueContainer, Icons.Filled.MoneyOff, R.string.alert_purchase_title)
}

@Composable
@Preview(showBackground = true)
private fun AlertPriceProfitPreview() {
    LucroVarejoTheme {
        AlertPrice(PREVIEW_PRICE, AlertPriceState.PROFIT)
    }
}

@Composable
@Preview(showBackground = true)
private fun AlertPricePurchasePreview() {
    LucroVarejoTheme {
        AlertPrice(PREVIEW_PRICE, AlertPriceState.PURCHASE)
    }
}

@Composable
@Preview(showBackground = true, name = "Lucro sem vendas")
private fun AlertPriceProfitEmptyPreview() {
    LucroVarejoTheme {
        AlertPrice(0.0, AlertPriceState.PROFIT)
    }
}

@Composable
@Preview(showBackground = true, name = "Custo sem vendas")
private fun AlertPricePurchaseEmptyPreview() {
    LucroVarejoTheme {
        AlertPrice(0.0, AlertPriceState.PURCHASE)
    }
}
