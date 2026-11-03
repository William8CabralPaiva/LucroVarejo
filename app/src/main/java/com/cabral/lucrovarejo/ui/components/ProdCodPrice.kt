package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProdCodPrice(
    name: String,
    code: String,
    cost: BigDecimal,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    ProdCodPriceContent(
        name = name,
        code = code,
        formattedCost = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(cost),
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun ProdCodPriceContent(
    name: String,
    code: String,
    formattedCost: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
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
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.last_sale_content_spacing)
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.last_sale_image_size))
                .clip(RoundedCornerShape(dimensionResource(R.dimen.last_sale_image_radius)))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(dimensionResource(R.dimen.last_sale_image_radius))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.last_sale_text_spacing)
            )
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.product_code_value, code),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formattedCost,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProdCodPricePreview() {
    LucroVarejoTheme {
        ProdCodPrice(
            name = "Bolsa Feminina",
            code = "7891000123456",
            cost = BigDecimal("89.90")
        )
    }
}