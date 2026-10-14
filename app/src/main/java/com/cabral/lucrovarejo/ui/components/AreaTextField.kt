package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.LocalIsDarkTheme
import com.cabral.lucrovarejo.R

@Composable
fun AreaTextField(
    modifier: Modifier = Modifier,
    initialValue: String = "",
    @StringRes labelResId: Int = R.string.product_description_label,
    @StringRes placeholderResId: Int = R.string.product_description_placeholder,
    onValueChange: (String) -> Unit = {},
) {
    var value by remember { mutableStateOf(initialValue) }
    AreaTextFieldContent(
        value = value,
        onValueChange = {
            value = it
            onValueChange(it)
        },
        modifier = modifier,
        labelResId = labelResId,
        placeholderResId = placeholderResId
    )
}

@Composable
fun AreaTextFieldContent(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes labelResId: Int = R.string.product_description_label,
    @StringRes placeholderResId: Int = R.string.product_description_placeholder,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    val shape = RoundedCornerShape(8.dp)
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
                text = stringResource(R.string.field_optional_marker),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, shape)
                .border(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outlineVariant,
                    shape = shape
                )
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = accentColor
                            )
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                    innerTextField()
                }
            )
        }
    }
}

internal fun formFieldAccentColor(
    colorScheme: ColorScheme,
    isDarkTheme: Boolean
): Color =
    if (isDarkTheme) {
        colorScheme.onSurface
    } else {
        colorScheme.primary
    }

@Preview(showBackground = true, name = "Descrição")
@Composable
private fun AreaTextFieldPreview() {
    LucroVarejoTheme {
        AreaTextField()
    }
}
