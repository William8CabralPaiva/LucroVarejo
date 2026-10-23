package com.cabral.lucrovarejo.ui.screens.cardbrands

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.CardBrandModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun CardBrandsScreen(
    onBackPress: () -> Unit = {},
    viewModel: CardBrandsViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()
    CardBrandsContent(
        state = state,
        onCreate = resolvedViewModel::onCreate,
        onRename = resolvedViewModel::onRename,
        onArchive = resolvedViewModel::onArchive,
    )
}

@Composable
fun CardBrandsContent(
    state: CardBrandsUiState,
    onCreate: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onArchive: (String) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var brandToRename by remember { mutableStateOf<CardBrandModel?>(null) }
    var brandToArchive by remember { mutableStateOf<CardBrandModel?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.card_brands_add)
                )
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.hasError -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.card_brands_load_error),
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.card_brands_empty))
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.key ?: it.name }) { brand ->
                    CardBrandItem(
                        brand = brand,
                        onClick = { brandToRename = brand },
                        onArchive = { brandToArchive = brand }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        CardBrandNameDialog(
            titleResId = R.string.card_brands_dialog_add_title,
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                onCreate(name)
                showAddDialog = false
            }
        )
    }

    brandToRename?.let { brand ->
        CardBrandNameDialog(
            titleResId = R.string.card_brands_dialog_rename_title,
            initialValue = brand.name,
            onDismiss = { brandToRename = null },
            onConfirm = { name ->
                brand.key?.let { onRename(it, name) }
                brandToRename = null
            }
        )
    }

    brandToArchive?.let { brand ->
        AlertDialog(
            onDismissRequest = { brandToArchive = null },
            title = { Text(stringResource(R.string.card_brands_archive_dialog_title)) },
            text = { Text(stringResource(R.string.card_brands_archive_dialog_message, brand.name)) },
            confirmButton = {
                TextButton(onClick = {
                    brand.key?.let(onArchive)
                    brandToArchive = null
                }) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { brandToArchive = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun CardBrandItem(
    brand: CardBrandModel,
    onClick: () -> Unit,
    onArchive: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = brand.name, style = MaterialTheme.typography.titleMedium)
                if (!brand.active) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.card_brands_archived_badge)) })
                }
            }
            if (brand.active) {
                TextButton(onClick = onArchive) {
                    Text(stringResource(R.string.card_brands_archive))
                }
            }
        }
    }
}

@Composable
private fun CardBrandNameDialog(
    titleResId: Int,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialValue) }
    val isError = name.isBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleResId)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    isError = isError,
                    label = { Text(stringResource(R.string.card_brands_name_label)) },
                    placeholder = { Text(stringResource(R.string.card_brands_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = stringResource(R.string.card_brands_name_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (!isError) onConfirm(name.trim()) }, enabled = !isError) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Preview(showBackground = true, name = "Bandeiras")
@Composable
private fun CardBrandsContentPreview() {
    LucroVarejoTheme {
        CardBrandsContent(
            state = CardBrandsUiState(
                isLoading = false,
                items = listOf(
                    CardBrandModel(key = "1", name = "Visa"),
                    CardBrandModel(key = "2", name = "Mastercard", active = false)
                )
            ),
            onCreate = {},
            onRename = { _, _ -> },
            onArchive = {}
        )
    }
}

@Preview(showBackground = true, name = "Bandeiras vazio")
@Composable
private fun CardBrandsContentEmptyPreview() {
    LucroVarejoTheme {
        CardBrandsContent(
            state = CardBrandsUiState(isLoading = false),
            onCreate = {},
            onRename = { _, _ -> },
            onArchive = {}
        )
    }
}
