package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.ui.theme.DarkPrimaryContainer
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.R

@Composable
fun Spinner(
    items: List<String>,
    modifier: Modifier = Modifier,
    initialValue: String? = null,
    @StringRes textId: Int,
    @StringRes placeholderResId: Int,
    required: Boolean = false,
    onItemSelected: (String) -> Unit = {},
) {
    var selectedItem by remember { mutableStateOf(initialValue) }
    SpinnerContent(
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

@Composable
fun SpinnerContent(
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
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (isError) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.outlineVariant
    val label = stringResource(textId)
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
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface, shape)
                    .border(1.dp, borderColor, shape)
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = DarkPrimaryContainer
                )
                Text(
                    text = selectedItem ?: placeholder,
                    color = if (selectedItem == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.spinner_open_description),
                    tint = DarkPrimaryContainer
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Produto")
@Composable
private fun SpinnerPreview() {
    LucroVarejoTheme {
        Spinner(
            textId = R.string.product,
            placeholderResId = R.string.product_placeholder,
            items = listOf(
                "Bolsa",
                "Mochila",
                "Acessório"
            ),
            initialValue = "Bolsa",
            modifier = Modifier.padding(16.dp)
        )
    }
}
