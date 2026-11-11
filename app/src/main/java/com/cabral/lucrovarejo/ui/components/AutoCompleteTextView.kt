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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LocalIsDarkTheme
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

const val AUTO_COMPLETE_TEXT_FIELD_TAG = "auto_complete_text_field"

/** Filtra [items] pelo texto digitado, ignorando maiusculas/minusculas. */
fun filterAutoCompleteItems(items: List<String>, query: String): List<String> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return items
    return items.filter { it.contains(trimmed, ignoreCase = true) }
}

@Composable
fun AutoCompleteTextView(
    items: ImmutableList<String>,
    @StringRes textId: Int,
    @StringRes placeholderResId: Int,
    modifier: Modifier = Modifier,
    initialValue: String? = null,
    required: Boolean = false,
    keyboardOptions: KeyboardOptions = AppKeyboardDefaults.common,
    onItemSelect: (String) -> Unit = {},
) {
    var selectedItem by remember { mutableStateOf(initialValue) }
    AutoCompleteTextView(
        selectedItem = selectedItem,
        items = items,
        onItemSelect = {
            selectedItem = it
            onItemSelect(it)
        },
        modifier = modifier,
        textId = textId,
        placeholderResId = placeholderResId,
        required = required,
        keyboardOptions = keyboardOptions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextView(
    selectedItem: String?,
    items: ImmutableList<String>,
    onItemSelect: (String) -> Unit,
    @StringRes textId: Int,
    @StringRes placeholderResId: Int,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = AppKeyboardDefaults.common,
    onTextChange: (String) -> Unit = {},
    itemKeys: ImmutableList<String>? = null,
    onItemKeySelect: (String) -> Unit = {},
    itemContent: (@Composable (String) -> Unit)? = null,
) {
    // Com itemKeys (paralelo a items), a selecao e o itemContent usam a chave, nao o texto.
    var expanded by remember { mutableStateOf(false) }
    var query by remember {
        val initialQuery = selectedItem.orEmpty()
        mutableStateOf(
            TextFieldValue(
                text = initialQuery,
                selection = TextRange(initialQuery.length)
            )
        )
    }
    LaunchedEffect(selectedItem) {
        if (selectedItem != null && selectedItem != query.text) {
            query = TextFieldValue(
                text = selectedItem,
                selection = TextRange(selectedItem.length)
            )
        }
    }
    val shape = RoundedCornerShape(dimensionResource(R.dimen.radius_8dp))
    val borderColor = if (isError) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.outlineVariant
    val accentColor = formFieldAccentColor(
        colorScheme = MaterialTheme.colorScheme,
        isDarkTheme = LocalIsDarkTheme.current
    )
    val label = stringResource(textId)
    val placeholder = stringResource(placeholderResId)
    val trimmedQuery = query.text.trim()
    val filteredIndices = items.indices.filter {
        trimmedQuery.isEmpty() || items[it].contains(trimmedQuery, ignoreCase = true)
    }

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
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { expanded = it && enabled }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape)
                    .border(dimensionResource(R.dimen.border_width_1dp), borderColor, shape)
                    .padding(horizontal = dimensionResource(R.dimen.spacing_16dp), vertical = dimensionResource(R.dimen.spacing_16dp)),
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
                        val textChanged = it.text != query.text
                        query = it
                        if (textChanged) {
                            expanded = true
                            onTextChange(it.text)
                        }
                    },
                    enabled = enabled,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(accentColor),
                    keyboardOptions = keyboardOptions,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = dimensionResource(R.dimen.spacing_16dp))
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled)
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
                expanded = expanded && enabled && filteredIndices.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                filteredIndices.forEach { index ->
                    val item = items[index]
                    val key = itemKeys?.getOrNull(index)
                    DropdownMenuItem(
                        text = { itemContent?.invoke(key ?: item) ?: Text(item) },
                        onClick = {
                            query = TextFieldValue(
                                text = item,
                                selection = TextRange(item.length)
                            )
                            if (key != null) onItemKeySelect(key) else onItemSelect(item)
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
            items = persistentListOf("Bolsa", "Mochila", "AcessÃ³rio"),
            initialValue = "Bolsa",
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))
        )
    }
}
