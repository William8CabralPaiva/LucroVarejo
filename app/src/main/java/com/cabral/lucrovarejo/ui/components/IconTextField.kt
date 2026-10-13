package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.ui.theme.DarkPrimaryContainer
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.R

enum class IconTextFieldType {
    TEXT,
    MONEY
}

@Composable
fun IconTextField(
    @StringRes labelResId: Int,
    @StringRes placeholderResId: Int,
    icon: ImageVector,
    type: IconTextFieldType,
    required: Boolean,
    modifier: Modifier = Modifier,
    initialValue: String = "",
    @StringRes iconContentDescriptionResId: Int? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
) {
    var value by remember { mutableStateOf(filterIconTextFieldInput(initialValue, type)) }

    IconTextFieldContent(
        value = value,
        onValueChange = { newValue ->
            val filteredValue = filterIconTextFieldInput(newValue, type)
            value = filteredValue
            onValueChange(filteredValue)
        },
        labelResId = labelResId,
        placeholderResId = placeholderResId,
        icon = icon,
        type = type,
        required = required,
        modifier = modifier,
        iconContentDescriptionResId = iconContentDescriptionResId,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText
    )
}

@Composable
fun IconTextFieldContent(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelResId: Int,
    @StringRes placeholderResId: Int,
    icon: ImageVector,
    type: IconTextFieldType,
    required: Boolean,
    modifier: Modifier = Modifier,
    @StringRes iconContentDescriptionResId: Int? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    val fieldShape = RoundedCornerShape(8.dp)
    val label = stringResource(labelResId)
    val placeholder = stringResource(placeholderResId)

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = DarkPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    if (required) R.string.field_required_marker
                    else R.string.field_optional_marker
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = if (required) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = filterIconTextFieldInput(value, type),
            onValueChange = { onValueChange(filterIconTextFieldInput(it, type)) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescriptionResId?.let {
                        stringResource(it)
                    },
                    tint = DarkPrimaryContainer
                )
            },
            supportingText = supportingText,
            isError = isError,
            enabled = enabled,
            singleLine = true,
            minLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (type == IconTextFieldType.MONEY) {
                    KeyboardType.Decimal
                } else {
                    KeyboardType.Text
                }
            ),
            shape = fieldShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, fieldShape)
                .border(
                    width = 1.dp,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = fieldShape
                )
        )
    }
}

internal fun filterIconTextFieldInput(value: String, type: IconTextFieldType): String =
    when (type) {
        IconTextFieldType.TEXT -> value
        IconTextFieldType.MONEY -> buildString {
            var decimalSeparatorAdded = false
            value.forEach { character ->
                when {
                    character in '0'..'9' -> append(character)
                    character == '.' && !decimalSeparatorAdded -> {
                        append(character)
                        decimalSeparatorAdded = true
                    }
                }
            }
        }
    }

@Preview(showBackground = true, name = "Campo de texto stateful")
@Composable
private fun IconTextFieldPreview() {
    LucroVarejoTheme {
        IconTextField(
            labelResId = R.string.product_name_label,
            placeholderResId = R.string.product_name_placeholder,
            icon = Icons.Outlined.ShoppingBag,
            type = IconTextFieldType.TEXT,
            required = true
        )
    }
}

@Preview(showBackground = true, name = "Campo de valor stateless")
@Composable
private fun IconTextFieldContentPreview() {
    LucroVarejoTheme {
        IconTextFieldContent(
            value = "50.00",
            onValueChange = {},
            labelResId = R.string.product_paid_value_label,
            placeholderResId = R.string.product_paid_value_placeholder,
            icon = Icons.Outlined.MonetizationOn,
            type = IconTextFieldType.MONEY,
            required = false
        )
    }
}
