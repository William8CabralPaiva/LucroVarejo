package com.cabral.lucrovarejo.ui.screens.addproduct

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.components.DELETE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.components.IconButton
import com.cabral.lucrovarejo.ui.components.AreaTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconTextFieldType
import com.cabral.lucrovarejo.ui.theme.DestructiveRed
import com.cabral.lucrovarejo.ui.theme.ForestGreenHeader
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun AddProductScreen(
    product: ProductModel? = null,
    onFinished: () -> Unit = {},
    viewModel: AddProductViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.product_create_success)

    LaunchedEffect(viewModel, product) {
        viewModel.loadProduct(product)
    }

    LaunchedEffect(viewModel) {
        viewModel.completions.collect { onFinished() }
    }

    LaunchedEffect(viewModel, successMessage) {
        viewModel.effects.collect {
            // Sem callback de atualizacao: as demais telas observam o Firestore em tempo real.
            snackBarHostState.showSnackbar(successMessage)
        }
    }

    AddProductContent(
        state = state,
        snackBarHostState = snackBarHostState,
        onNameChange = viewModel::onNameChanged,
        onCodeChange = viewModel::onCodeChanged,
        onCostChange = viewModel::onCostChanged,
        onDescriptionChange = viewModel::onDescriptionChanged,
        onSave = viewModel::saveProduct,
        onDelete = viewModel::onDeleteRequested,
        onDeleteConfirm = viewModel::confirmDelete,
        onDeleteDismiss = viewModel::onDeleteDismissed
    )
}

@Composable
fun AddProductContent(
    state: AddProductUiState,
    snackBarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit = {},
    onDeleteConfirm: () -> Unit = {},
    onDeleteDismiss: () -> Unit = {}
) {
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text(stringResource(R.string.product_delete_dialog_title)) },
            text = {
                Text(stringResource(R.string.product_delete_dialog_message, state.name))
            },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
                    Text(
                        text = stringResource(R.string.product_delete_dialog_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) {
                    Text(stringResource(R.string.date_picker_cancel))
                }
            }
        )
    }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(R.dimen.register_screen_padding)),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.register_field_spacing)
                )
            ) {
                IconTextFieldContent(
                    value = state.name,
                    onValueChange = onNameChange,
                    labelResId = R.string.product_name_label,
                    placeholderResId = R.string.product_name_placeholder,
                    icon = Icons.Outlined.ShoppingBag,
                    type = IconTextFieldType.TEXT,
                    required = true,
                    enabled = !state.isLoading,
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { errorId ->
                        { Text(stringResource(errorId)) }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                IconTextFieldContent(
                    value = state.cod,
                    onValueChange = onCodeChange,
                    labelResId = R.string.product_code_label,
                    placeholderResId = R.string.product_code_placeholder,
                    icon = Icons.Outlined.Numbers,
                    type = IconTextFieldType.TEXT,
                    required = false,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(4.dp))

                IconTextFieldContent(
                    value = state.cost,
                    onValueChange = onCostChange,
                    labelResId = R.string.product_paid_value_label,
                    placeholderResId = R.string.product_paid_value_placeholder,
                    icon = Icons.Outlined.MonetizationOn,
                    type = IconTextFieldType.MONEY,
                    required = true,
                    enabled = !state.isLoading,
                    isError = state.costError != null,
                    supportingText = state.costError?.let { errorId ->
                        { Text(stringResource(errorId)) }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                AreaTextFieldContent(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    labelResId = R.string.product_description_label,
                    placeholderResId = R.string.product_description_placeholder,
                    enabled = !state.isLoading
                )

                state.errorMessage?.let { messageId ->
                    Text(
                        text = stringResource(messageId),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))
                if (state.isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            enabled = !state.isLoading,
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Delete,
                            text = stringResource(R.string.product_delete_submit),
                            containerColor = DestructiveRed,
                            testTag = DELETE_PRODUCT_BUTTON_TAG
                        )
                        IconButton(
                            enabled = state.canSubmit,
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            isLoading = state.isLoading,
                            icon = Icons.Default.Edit,
                            text = stringResource(R.string.product_edit_submit),
                            loadingText = stringResource(R.string.product_edit_loading)
                        )
                    }
                } else {
                    IconButton(
                        enabled = state.canSubmit,
                        onClick = onSave,
                        isLoading = state.isLoading
                    )
                }
            }

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = dimensionResource(R.dimen.product_snackbar_horizontal_padding),
                        vertical = dimensionResource(R.dimen.product_snackbar_vertical_padding)
                    ),
                snackbar = { snackBarData ->
                    Snackbar(
                        snackbarData = snackBarData,
                        containerColor = ForestGreenHeader,
                        contentColor = Color.White
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900, name = "Editar produto")
@Composable
private fun EditProductScreenPreview() {
    LucroVarejoTheme {
        AddProductContent(
            state = AddProductUiState(
                key = "1",
                name = "Bolsa Feminina",
                cod = "7891000123456",
                cost = "89.90",
                description = "Bolsa de couro"
            ),
            snackBarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onCodeChange = {},
            onCostChange = {},
            onDescriptionChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900, name = "Confirmar exclusão")
@Composable
private fun DeleteProductDialogPreview() {
    LucroVarejoTheme {
        AddProductContent(
            state = AddProductUiState(
                key = "1",
                name = "Bolsa Feminina",
                cod = "7891000123456",
                cost = "89.90",
                description = "Bolsa de couro",
                showDeleteDialog = true
            ),
            snackBarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onCodeChange = {},
            onCostChange = {},
            onDescriptionChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddProductScreenPreview() {
    LucroVarejoTheme {
        AddProductContent(
            state = AddProductUiState(),
            snackBarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onCodeChange = {},
            onCostChange = {},
            onDescriptionChange = {},
            onSave = {}
        )
    }
}