package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.DestructiveRed
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.MediumGreenCard
import com.cabral.lucrovarejo.ui.theme.TextPrimaryDark

@Composable
fun AlertPurchase(
    price: Double,
    productsSize: Int,
    modifier: Modifier = Modifier,
    titleRes: Int = R.string.alert_month_purchase,
    icon: ImageVector = Icons.Outlined.MonetizationOn,
    containerColor: Color = MediumGreenCard,
    contentColor: Color = TextPrimaryDark,
    showProductsInfo: Boolean = true,
    showTrendIcon: Boolean = true,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ALERT_PURCHASE_TAG),
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Row(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = stringResource(id = titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatAlertPrice(price),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = contentColor
                    )
                    if (showProductsInfo) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (!showProductsInfo) {
                        return@Column
                    }
                    if (productsSize > 0) {
                        Text(
                            text = "$productsSize ${stringResource(id = R.string.sale_products)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.alert_no_products),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor
                        )
                    }
                }
            }
            if (showTrendIcon) {
                Row {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

    }
}

internal const val ALERT_PURCHASE_TAG = "alert-purchase"
private const val PREVIEW_PRICE = 50.90
private const val PREVIEW_PRODUCTS_SIZE = 10

@Composable
@Preview(showBackground = true)
private fun AlertPurchasePreview() {
    LucroVarejoTheme {
        AlertPurchase(PREVIEW_PRICE, PREVIEW_PRODUCTS_SIZE)
    }
}

@Composable
@Preview(showBackground = true, name = "Tarifas da Maquininha")
private fun AlertPurchaseTariffPreview() {
    LucroVarejoTheme {
        AlertPurchase(
            price = 12.50,
            productsSize = 0,
            titleRes = R.string.alert_tariff_title,
            icon = Icons.Outlined.PointOfSale,
            containerColor = DestructiveRed,
            contentColor = Color.White,
            showProductsInfo = false,
            showTrendIcon = false
        )
    }
}

@Composable
@Preview(showBackground = true, name = "Vendas do mês sem vendas")
private fun AlertPurchaseEmptyPreview() {
    LucroVarejoTheme {
        AlertPurchase(price = 0.0, productsSize = 0)
    }
}
