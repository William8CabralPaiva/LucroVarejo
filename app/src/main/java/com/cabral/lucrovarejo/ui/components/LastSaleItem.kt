package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun LastSaleItem(
    productName: String,
    priceInCents: Long,
    timestamp: Timestamp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null
) {
    val todayLabel = stringResource(R.string.last_sale_today)
    val yesterdayLabel = stringResource(R.string.last_sale_yesterday)
    val formattedDate = formatLastSaleTimestamp(timestamp, todayLabel, yesterdayLabel)
    LastSaleItemContent(
        productName = productName,
        formattedPrice = formatAlertPrice(priceInCents / 100.0),
        formattedDate = formattedDate,
        onClick = onClick,
        modifier = modifier,
        image = image
    )
}

@Composable
fun LastSaleItemContent(
    productName: String,
    formattedPrice: String,
    formattedDate: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(dimensionResource(R.dimen.last_sale_card_radius))
            ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.last_sale_card_radius)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.last_sale_card_padding)),
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
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
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
}

internal fun formatLastSaleTimestamp(
    timestamp: Timestamp,
    todayLabel: String,
    yesterdayLabel: String,
    now: Date = Date(),
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    val saleDate = timestamp.toDate()
    val saleDay = Calendar.getInstance(timeZone).apply { time = saleDate }
    val currentDay = Calendar.getInstance(timeZone).apply { time = now }

    val dayLabel = when {
        isSameDay(saleDay, currentDay) -> todayLabel
        isPreviousDay(saleDay, currentDay) -> yesterdayLabel
        else -> SimpleDateFormat("dd/MM", Locale.forLanguageTag("pt-BR")).apply {
            this.timeZone = timeZone
        }.format(saleDate)
    }
    return dayLabel
}

private fun isSameDay(first: Calendar, second: Calendar): Boolean =
    first.get(Calendar.ERA) == second.get(Calendar.ERA) &&
        first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)

private fun isPreviousDay(saleDay: Calendar, currentDay: Calendar): Boolean {
    val yesterday = (currentDay.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(saleDay, yesterday)
}

@Composable
@Preview(showBackground = true, name = "Última venda")
fun LastSaleItemPreview() {
    LucroVarejoTheme {
        LastSaleItem(
            productName = "Bolsa Feminina",
            priceInCents = 8990,
            timestamp = Timestamp.now(),
            onClick = {}
        )
    }
}