@file:Suppress("MatchingDeclarationName")

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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.LocalIsDarkTheme
import com.cabral.lucrovarejo.R

enum class IconTextFieldType {
    TEXT,
    MONEY,
    // Moeda: sempre com 2 casas ("50,00"); o estado guarda o decimal simples ("50.00").
    CURRENCY
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
    keyboardOptions: KeyboardOptions? = null,
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
        supportingText = supportingText,
        keyboardOptions = keyboardOptions
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
    keyboardOptions: KeyboardOptions? = null,
) {
    val fieldShape = RoundedCornerShape(dimensionResource(R.dimen.radius_8dp))
    val label = stringResource(labelResId)
    val placeholder = stringResource(placeholderResId)
    val accentColor = formFieldAccentColor(
        colorScheme = MaterialTheme.colorScheme,
        isDarkTheme = LocalIsDarkTheme.current
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = accentColor,
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
                modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_4dp))
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_4dp)))
        val displayText = if (type == IconTextFieldType.CURRENCY) {
            plainToCurrencyDisplay(value)
        } else {
            filterIconTextFieldInput(value, type)
        }
        var field by remember {
            mutableStateOf(
                TextFieldValue(
                    displayText,
                    TextRange(displayText.indexOf(',').takeIf { it >= 0 } ?: displayText.length)
                )
            )
        }
        if (field.text != displayText) {
            field = TextFieldValue(displayText, TextRange(displayText.length))
        }
        TextField(
            value = field,
            onValueChange = { newField ->
                if (type == IconTextFieldType.CURRENCY) {
                    val input = applyCurrencyInput(newField.text, newField.selection.end)
                    field = TextFieldValue(input.display, TextRange(input.cursor))
                    onValueChange(input.plain)
                } else {
                    field = newField
                    onValueChange(filterIconTextFieldInput(newField.text, type))
                }
            },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescriptionResId?.let {
                        stringResource(it)
                    },
                    tint = accentColor
                )
            },
            supportingText = supportingText,
            isError = isError,
            enabled = enabled,
            singleLine = true,
            minLines = 1,
            keyboardOptions = keyboardOptions ?: when (type) {
                IconTextFieldType.TEXT -> AppKeyboardDefaults.common
                IconTextFieldType.MONEY,
                IconTextFieldType.CURRENCY -> AppKeyboardDefaults.decimal
            },
            shape = fieldShape,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
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
                .background(MaterialTheme.colorScheme.surfaceVariant, fieldShape)
                .border(
                    width = dimensionResource(R.dimen.border_width_1dp),
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

// Resultado da digitação em campo de moeda: decimal simples ("50.00"), texto exibido ("50,00") e cursor.
internal data class CurrencyInput(val plain: String, val display: String, val cursor: Int)

// Digitação natural com ",00" sempre visível: "5" -> "5,00"; "50" -> "50,00".
// Dígitos antes da vírgula editam os reais; depois da vírgula, os centavos (máx. 2).
// Digitar "," ou "." leva o cursor para os centavos. Apagar tudo limpa o campo.
internal fun applyCurrencyInput(text: String, cursor: Int): CurrencyInput {
    val caret = cursor.coerceIn(0, text.length)
    val separatorIndex = text.indexOfFirst { it == ',' || it == '.' }
    val typedSeparator = caret > 0 && (text[caret - 1] == ',' || text[caret - 1] == '.')
    val integerDigits = (if (separatorIndex < 0) text else text.substring(0, separatorIndex))
        .filter { it in '0'..'9' }
    val decimalDigits = (if (separatorIndex < 0) "" else text.substring(separatorIndex + 1))
        .filter { it in '0'..'9' }
        .take(CURRENCY_DECIMAL_DIGITS)

    if (integerDigits.isEmpty() && !typedSeparator && decimalDigits.all { it == '0' }) {
        return CurrencyInput(plain = "", display = "", cursor = 0)
    }

    val integer = integerDigits.trimStart('0').take(MAX_CURRENCY_INTEGER_DIGITS)
    val integerOut = integer.ifEmpty { "0" }
    val decimalsOut = decimalDigits.padEnd(CURRENCY_DECIMAL_DIGITS, '0')

    val newCursor = when {
        typedSeparator -> integerOut.length + 1
        integer.isEmpty() -> integerOut.length
        separatorIndex < 0 || caret <= separatorIndex -> {
            val digitsBefore = text.substring(0, caret).count { it in '0'..'9' }
            val stripped = integerDigits.length - integerDigits.trimStart('0').length
            (digitsBefore - stripped).coerceIn(0, integerOut.length)
        }
        else -> {
            val decimalsBefore = text.substring(separatorIndex + 1, caret).count { it in '0'..'9' }
            integerOut.length + 1 + minOf(decimalsBefore, CURRENCY_DECIMAL_DIGITS)
        }
    }
    return CurrencyInput(
        plain = "$integerOut.$decimalsOut",
        display = "$integerOut,$decimalsOut",
        cursor = newCursor
    )
}

// "89.9" -> "89,90"; valor vazio ou inválido -> "".
internal fun plainToCurrencyDisplay(plain: String): String =
    plain.toBigDecimalOrNull()
        ?.setScale(CURRENCY_DECIMAL_DIGITS, java.math.RoundingMode.HALF_UP)
        ?.toPlainString()
        ?.replace('.', ',')
        ?: ""
private const val MAX_CURRENCY_INTEGER_DIGITS = 10
private const val CURRENCY_DECIMAL_DIGITS = 2

internal fun filterIconTextFieldInput(value: String, type: IconTextFieldType): String =
    when (type) {
        IconTextFieldType.TEXT, IconTextFieldType.CURRENCY -> value
        IconTextFieldType.MONEY -> buildString {
            var decimalSeparatorAdded = false
            value.forEach { character ->
                when {
                    character in '0'..'9' -> append(character)
                    (character == '.' || character == ',') && !decimalSeparatorAdded -> {
                        append(character)
                        decimalSeparatorAdded = true
                    }
                }
            }
        }.replace(',', '.')
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
