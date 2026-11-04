package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.cabral.lucrovarejo.ui.screens.register.RegisterValidator
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun SwitchStoreDialog(
    stores: List<StoreSummary>,
    activeStoreId: String?,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_switch_store_dialog_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                stores.forEach { store ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = store.id == activeStoreId,
                                role = Role.RadioButton,
                                onClick = { onSelect(store.id) }
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = store.id == activeStoreId, onClick = null)
                        Text(
                            text = store.displayName.ifBlank {
                                stringResource(R.string.settings_store_unnamed)
                            },
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreate) {
                Text(stringResource(R.string.settings_create_store))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
fun StoreNameDialog(
    initialName: String,
    @StringRes title: Int,
    @StringRes errorMessage: Int,
    onSave: (String, (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var isSaving by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    val validationError = RegisterValidator.validateDisplayName(name)

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(MAX_STORE_NAME_LENGTH)
                    hasError = false
                },
                label = { Text(stringResource(R.string.register_display_name_label)) },
                singleLine = true,
                enabled = !isSaving,
                isError = hasError || (name.isNotEmpty() && validationError != null),
                supportingText = {
                    when {
                        hasError -> Text(stringResource(errorMessage))
                        name.isNotEmpty() && validationError != null ->
                            Text(stringResource(validationError))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = validationError == null && !isSaving,
                onClick = {
                    isSaving = true
                    onSave(name) { success ->
                        isSaving = false
                        if (success) onDismiss() else hasError = true
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

private const val MAX_STORE_NAME_LENGTH = 100

@Preview(showBackground = true, name = "Trocar loja")
@Composable
private fun SwitchStoreDialogPreview() {
    LucroVarejoTheme {
        SwitchStoreDialog(
            stores = listOf(
                StoreSummary("1", "Loja Centro"),
                StoreSummary("2", "Loja Shopping"),
                StoreSummary("3", "")
            ),
            activeStoreId = "1",
            onSelect = {},
            onCreate = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Nova loja")
@Composable
private fun StoreNameDialogPreview() {
    LucroVarejoTheme {
        StoreNameDialog(
            initialName = "",
            title = R.string.settings_create_store_dialog_title,
            errorMessage = R.string.settings_create_store_error,
            onSave = { _, _ -> },
            onDismiss = {}
        )
    }
}
