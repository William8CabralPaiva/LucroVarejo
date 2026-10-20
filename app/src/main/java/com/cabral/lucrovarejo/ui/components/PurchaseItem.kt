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
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

// Valores em centavos, como no PurchaseModel; preço, custo e lucro são totais da venda.
@Composable
fun PurchaseItem(
    productName: String,
    salePriceInCents: Long,
    costInCents: Long,
    quantity: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null
) {
    val profitInCents = salePriceInCents - costInCents
    PurchaseItemContent(
        productName = productName,
        formattedPrice = formatAlertPrice(salePriceInCents / 100.0),
        formattedCost = formatAlertPrice(costInCents / 100.0),
        formattedProfit = formatAlertPrice(profitInCents / 100.0),
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, cardShape)
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
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            Text(
                text = formattedPrice,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    R.string.purchase_item_cost_profit,
                    formattedCost,
                    formattedProfit
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.purchase_item_quantity, quantity),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

    }
}

@Composable
@Preview(showBackground = true, name = "Venda")
fun PurchaseItemPreview() {
    LucroVarejoTheme {
        PurchaseItem(
            productName = "Bolsa Feminina",
            salePriceInCents = 8990,
            costInCents = 5000,
            quantity = 1,
            onClick = {}
        )
    }
}

