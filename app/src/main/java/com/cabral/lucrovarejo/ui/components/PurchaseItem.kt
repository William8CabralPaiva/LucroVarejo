package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LocalIsDarkTheme
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ProfitGreen

// Valores em centavos, como no PurchaseModel; preço, custo, tarifa e lucro são totais da venda.
// O lucro já desconta a tarifa.
@Composable
fun PurchaseItem(
    productName: String,
    salePriceInCents: Long,
    costInCents: Long,
    quantity: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tariffInCents: Long = 0L,
    image: Painter? = null
) {
    val profitInCents = salePriceInCents - costInCents - tariffInCents
    PurchaseItemContent(
        productName = productName,
        formattedPrice = formatAlertPrice(salePriceInCents / CENTS_PER_UNIT),
        formattedCost = formatAlertPrice(costInCents / CENTS_PER_UNIT),
        formattedTariff = formatAlertPrice(tariffInCents / CENTS_PER_UNIT),
        formattedProfit = formatAlertPrice(profitInCents / CENTS_PER_UNIT),
        quantity = quantity,
        onClick = onClick,
        modifier = modifier,
        image = image
    )
}

@Composable
fun PurchaseItemContent(
    productName: String,
    formattedPrice: String,
    formattedCost: String,
    formattedTariff: String,
    formattedProfit: String,
    quantity: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null
) {
    val cardShape = RoundedCornerShape(dimensionResource(R.dimen.last_sale_card_radius))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surface, cardShape)
            .border(dimensionResource(R.dimen.border_width_1dp), MaterialTheme.colorScheme.outlineVariant, cardShape)
            .clickable(onClick = onClick)
            .padding(dimensionResource(R.dimen.last_sale_card_padding)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.last_sale_content_spacing)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.last_sale_image_size))
                .clip(RoundedCornerShape(dimensionResource(R.dimen.last_sale_image_radius)))
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.last_sale_image_radius))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                Image(
                    painter = image,
                    contentDescription = productName,
                    modifier = Modifier.size(dimensionResource(R.dimen.last_sale_image_size)),
                    contentScale = ContentScale.Crop
                )
            } else {
                ProductIcon(text = productName)
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.last_sale_text_spacing))
        ) {
            Text(
                text = productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.purchase_item_price_quantity, formattedPrice, quantity),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.purchase_item_profit, formattedProfit),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (LocalIsDarkTheme.current) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        ProfitGreen
                    }
                )
            }
            Text(
                text = stringResource(R.string.purchase_item_cost_tariff, formattedCost, formattedTariff),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@Preview(showBackground = true, name = "Venda")
private fun PurchaseItemPreview() {
    LucroVarejoTheme {
        PurchaseItem(
            productName = "Bolsa Feminina",
            salePriceInCents = 8990,
            costInCents = 5000,
            tariffInCents = 224,
            quantity = 1,
            onClick = {}
        )
    }
}

