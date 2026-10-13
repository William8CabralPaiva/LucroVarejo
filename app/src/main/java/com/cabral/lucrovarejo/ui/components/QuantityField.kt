package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.ui.theme.DarkPrimaryContainer
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.R

private const val MIN_QUANTITY = 1
private const val MAX_QUANTITY = 100

@Composable
fun QuantityField(
    modifier: Modifier = Modifier,
    initialValue: Int = MIN_QUANTITY,
    @StringRes labelResId: Int = R.string.product_quantity_label,
    onValueChange: (Int) -> Unit = {},
) {
    var quantity by remember { mutableIntStateOf(initialValue.coerceIn(MIN_QUANTITY, MAX_QUANTITY)) }

    QuantityFieldContent(
        value = quantity,
        onValueChange = { newValue ->
            quantity = newValue.coerceIn(MIN_QUANTITY, MAX_QUANTITY)
            onValueChange(quantity)
        },
        labelResId = labelResId,
        modifier = modifier
    )
}

@Composable
fun QuantityFieldContent(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes labelResId: Int = R.string.product_quantity_label,
) {
    val quantity = value.coerceIn(MIN_QUANTITY, MAX_QUANTITY)
    val shape = RoundedCornerShape(8.dp)
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val label = stringResource(labelResId)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = DarkPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface, shape)
                    .border(1.dp, borderColor, shape)
                    .padding(start = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = DarkPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = quantity.toString(),
                    onValueChange = { input ->
                        val digits = input.filter { it in '0'..'9' }.take(3)
                        val parsed = digits.toIntOrNull()
                        if (parsed != null && parsed in MIN_QUANTITY..MAX_QUANTITY) {
                            onValueChange(parsed)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            QuantityButton(
                descriptionResId = R.string.quantity_decrease_description,
                enabled = quantity > MIN_QUANTITY,
                icon = Icons.Filled.Remove,
                shape = shape,
                onClick = { onValueChange(quantity - 1) }
            )
            QuantityButton(
                descriptionResId = R.string.quantity_increase_description,
                enabled = quantity < MAX_QUANTITY,
                icon = Icons.Filled.Add,
                shape = shape,
                onClick = { onValueChange(quantity + 1) }
            )
        }
    }
}

@Composable
private fun QuantityButton(
    @StringRes descriptionResId: Int,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(descriptionResId),
            tint = if (enabled) DarkPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, name = "Quantidade")
@Composable
private fun QuantityFieldPreview() {
    LucroVarejoTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            QuantityField(initialValue = 1)
        }
    }
}
