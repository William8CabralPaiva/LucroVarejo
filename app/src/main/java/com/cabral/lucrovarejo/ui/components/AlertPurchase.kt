package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.MediumGreenCard
import com.cabral.lucrovarejo.ui.theme.TextPrimaryDark

@Composable
fun AlertPurchase(
    price: Double,
    productsSize: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag(ALERT_PURCHASE_TAG),
        shape = RoundedCornerShape(8.dp),
        color = MediumGreenCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Icon(
                    imageVector = Icons.Outlined.MonetizationOn,
                    contentDescription = null,
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Row {
                Column {
                    Text(
                        text = stringResource(id = R.string.alert_month_purchase),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatAlertPrice(price),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$productsSize ${stringResource(id = R.string.sale_products)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimaryDark
                    )
                }
            }
            Row {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

    }
}

internal const val ALERT_PURCHASE_TAG = "alert-purchase"

@Composable
@Preview(showBackground = true)
fun AlertPurchasePreview() {
    LucroVarejoTheme {
        AlertPurchase(50.90, 10)
    }
}
