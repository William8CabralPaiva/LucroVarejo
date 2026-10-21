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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LocalIsDarkTheme
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

const val AUTO_COMPLETE_TEXT_FIELD_TAG = "auto_complete_text_field"

/** Filtra [items] pelo texto digitado, ignorando maiusculas/minusculas. */
fun filterAutoCompleteItems(items: List<String>, query: String): List<String> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return items
    return items.filter { it.contains(trimmed, ignoreCase = true) }
}

@Composable
fun AutoCompleteTextView(
    items: List<String>,
    modifier: Modifier = Modifier,
    initialValue: String? = null,
    @StringRes textId: Int,
    @StringRes placeholderResId: Int,
    required: Boolean = false,
    onItemSelected: (String) -> Unit = {},
) {
    var selectedItem by remember { mutableStateOf(initialValue) }
    AutoCompleteTextViewContent(
        selectedItem = selectedItem,
        items = items,
        onItemSelected = {
            selectedItem = it
            onItemSelected(it)
        },
        modifier = modifier,
        textId = textId,
        placeholderResId = placeholderResId,
        required = required
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextViewContent(
    selectedItem: String?,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes textId: Int,
    @StringRes placeholderResId: Int,
    required: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember(selectedItem) {
        val initialQuery = selectedItem.orEmpty()
        mutableStateOf(
            TextFieldValue(
                text = initialQuery,
                selection = TextRange(initialQuery.length)
            )
        )
    }
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (isError) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.outlineVariant
    val accentColor = formFieldAccentColor(
        colorScheme = MaterialTheme.colorScheme,
        isDarkTheme = LocalIsDarkTheme.current
    )
    val label = stringResource(textId)
    val placeholder = stringResource(placeholderResId)
    val filteredItems = filterAutoCompleteItems(items, query.text)

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
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { expanded = it && enabled }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape)
                    .border(1.dp, borderColor, shape)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = accentColor
                )
                BasicTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        expanded = true
                    },
                    enabled = enabled,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled)
                        .onFocusChanged { if (it.isFocused) expanded = true }
                        .testTag(AUTO_COMPLETE_TEXT_FIELD_TAG),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                        if (query.text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
            ExposedDropdownMenu(
                expanded = expanded && enabled && filteredItems.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                filteredItems.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            query = TextFieldValue(
                                text = item,
                                selection = TextRange(item.length)
                            )
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Produto autocomplete")
@Composable
private fun AutoCompleteTextViewPreview() {
    LucroVarejoTheme {
        AutoCompleteTextView(
            textId = R.string.product,
            placeholderResId = R.string.product_placeholder,
            items = listOf("Bolsa", "Mochila", "AcessÃ³rio"),
            initialValue = "Bolsa",
            modifier = Modifier.padding(16.dp)
        )
    }
}
