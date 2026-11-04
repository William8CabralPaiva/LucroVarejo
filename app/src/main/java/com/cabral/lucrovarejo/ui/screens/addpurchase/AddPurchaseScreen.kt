package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MonetizationOn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.payment.bpsToRate
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.ui.components.AutoCompleteTextViewContent
import com.cabral.lucrovarejo.ui.components.DELETE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.components.DateTextField
import com.cabral.lucrovarejo.ui.components.IconTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconTextFieldType
import com.cabral.lucrovarejo.ui.components.LoadingFullScreen
import com.cabral.lucrovarejo.ui.components.QuantityFieldContent
import com.cabral.lucrovarejo.ui.components.SaveProductButtonContent
import com.cabral.lucrovarejo.ui.components.SpinnerContent
import com.cabral.lucrovarejo.ui.payment.PaymentMethodsUi
import com.cabral.lucrovarejo.ui.theme.ForestGreenHeader
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import kotlinx.collections.immutable.toImmutableList
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AddPurchaseScreen(
    purchase: PurchaseModel? = null,
    onFinish: () -> Unit = {},
    viewModel: AddPurchaseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.purchase_create_success)
    val currentOnFinish by rememberUpdatedState(onFinish)
    LaunchedEffect(viewModel, purchase) {
        viewModel.loadPurchase(purchase)
    }
    LaunchedEffect(viewModel) {
        viewModel.completions.collect { currentOnFinish() }
    }
    LaunchedEffect(viewModel, successMessage) {
        viewModel.effects.collect {
            // Sem callback de atualizacao: o Home observa o Firestore e se atualiza sozinho.
            snackBarHostState.showSnackbar(successMessage)
        }
    }
    AddPurchaseScreenContent(
        state = state,
        snackBarHostState = snackBarHostState,
        onProductSelect = viewModel::onProductSelected,
        onProductTextChange = viewModel::onProductTextChanged,
        onSalePriceChange = viewModel::onSalePriceChanged,
        onQuantityChange = viewModel::onQuantityChanged,
        onPaymentMethodSelect = viewModel::onPaymentMethodSelected,
        onTerminalSelect = viewModel::onTerminalSelected,
        onBrandSelect = viewModel::onBrandSelected,
        onInstallmentsSelect = viewModel::onInstallmentsSelected,
        onSaleDateChange = viewModel::onSaleDateChanged,
        onSave = viewModel::savePurchase,
        onDelete = viewModel::onDeleteRequested,
        onDeleteConfirm = viewModel::confirmDelete,
        onDeleteDismiss = viewModel::onDeleteDismissed
    )
}

@Composable
fun AddPurchaseScreenContent(
    state: AddPurchaseUiState,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onProductSelect: (String) -> Unit = {},
    onProductTextChange: (String) -> Unit = {},
    onSalePriceChange: (String) -> Unit = {},
    onQuantityChange: (Int) -> Unit = {},
    onPaymentMethodSelect: (PaymentMethods) -> Unit = {},
    onTerminalSelect: (String) -> Unit = {},
    onBrandSelect: (String) -> Unit = {},
    onInstallmentsSelect: (Int) -> Unit = {},
    onSaleDateChange: (Long) -> Unit = {},
    onSave: () -> Unit = {},
    onDelete: () -> Unit = {},
    onDeleteConfirm: () -> Unit = {},
    onDeleteDismiss: () -> Unit = {}
) {
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text(stringResource(R.string.purchase_delete_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.purchase_delete_dialog_message,
                        state.form.selectedProduct?.name.orEmpty()
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
                    Text(
                        text = stringResource(R.string.purchase_delete_dialog_confirm),
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
    val paymentMethods = PaymentMethodsUi.entries
    val paymentLabels = remember { paymentMethods.map { it.displayName }.toImmutableList() }
    val productNames = remember(state.products) { state.products.map(::productDisplayName).toImmutableList() }
    val brandOptions = remember(state.brandOptions) { state.brandOptions.toImmutableList() }
    val form = state.form
    val selectedPaymentLabel = paymentMethods
        .firstOrNull { it.method == form.paymentMethod }
        ?.displayName
    val salePriceError = form.salePriceError?.let { stringResource(it) }
    val errorMessage = state.errorMessage?.let { stringResource(it) }
    val tariffNotice = state.tariffNotice?.let { stringResource(it) }
    val installmentLabels = state.installmentOptions.map {
        stringResource(R.string.purchase_installments_option, it)
    }.toImmutableList()
    val terminalNames = remember(state.terminalOptions) {
        state.terminalOptions.map { it.name }.toImmutableList()
    }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")) }
    val profitText = state.profit?.let { profit ->
        remember(profit) {
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(profit)
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoadingProducts) {
            LoadingFullScreen(
                loadingTextId = R.string.purchase_load_products_loading
            )
        } else key(state.formResetCount) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(R.dimen.register_screen_padding)),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.register_field_spacing)
                )
            ) {
                AutoCompleteTextViewContent(
                    selectedItem = form.selectedProduct?.let(::productDisplayName),
                    items = productNames,
                    onItemSelect = onProductSelect,
                    onTextChange = onProductTextChange,
                    textId = R.string.product,
                    placeholderResId = R.string.product_placeholder,
                    required = true,
                    enabled = !state.isSaving
                )

                if (state.products.isEmpty() && state.errorMessage == null) {
                    Text(
                        text = stringResource(R.string.purchase_no_products),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconTextFieldContent(
                    value = form.salePrice,
                    onValueChange = onSalePriceChange,
                    labelResId = R.string.purchase_sale_price_label,
                    placeholderResId = R.string.product_paid_value_placeholder,
                    icon = Icons.Outlined.MonetizationOn,
                    type = IconTextFieldType.CURRENCY,
                    required = true,
                    enabled = !state.isSaving,
                    isError = form.salePriceError != null,
                    supportingText = salePriceError?.let { message ->
                        { Text(message) }
                    }
                )

                QuantityFieldContent(
                    value = form.quantity,
                    onValueChange = onQuantityChange
                )

                SpinnerContent(
                    selectedItem = selectedPaymentLabel,
                    items = paymentLabels,
                    onItemSelect = { selectedLabel ->
                        paymentMethods.firstOrNull { it.displayName == selectedLabel }
                            ?.method
                            ?.let(onPaymentMethodSelect)
                    },
                    textId = R.string.purchase_payment_method_label,
                    placeholderResId = R.string.purchase_payment_method_placeholder,
                    required = true,
                    enabled = !state.isSaving
                )

                if (state.showTerminal) {
                    SpinnerContent(
                        selectedItem = form.terminalName,
                        items = terminalNames,
                        onItemSelect = onTerminalSelect,
                        textId = R.string.purchase_terminal_label,
                        placeholderResId = R.string.purchase_terminal_placeholder,
                        required = true,
                        enabled = !state.isSaving && terminalNames.isNotEmpty()
                    )
                    if (terminalNames.isEmpty() && !state.isLoadingTariffs) {
                        Text(
                            text = stringResource(R.string.purchase_terminal_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (state.showBrand) {
                    SpinnerContent(
                        selectedItem = form.brand,
                        items = brandOptions,
                        onItemSelect = onBrandSelect,
                        textId = R.string.purchase_brand_label,
                        placeholderResId = R.string.purchase_brand_placeholder,
                        required = true,
                        enabled = !state.isSaving && form.terminalId != null &&
                                state.brandOptions.isNotEmpty()
                    )
                    if (form.terminalId != null && state.brandOptions.isEmpty() &&
                        !state.isLoadingTariffs
                    ) {
                        Text(
                            text = stringResource(R.string.purchase_brand_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (state.showInstallments) {
                    SpinnerContent(
                        selectedItem = form.installments?.let {
                            stringResource(R.string.purchase_installments_option, it)
                        },
                        items = installmentLabels,
                        onItemSelect = { label ->
                            state.installmentOptions
                                .getOrNull(installmentLabels.indexOf(label))
                                ?.let(onInstallmentsSelect)
                        },
                        textId = R.string.purchase_installments_label,
                        placeholderResId = R.string.purchase_installments_placeholder,
                        required = true,
                        enabled = !state.isSaving && form.brand != null &&
                                state.installmentOptions.isNotEmpty()
                    )
                }

                tariffNotice?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }

                DateTextField(
                    initialDateMillis = form.saleDateMillis,
                    onDateChange = onSaleDateChange
                )

                form.selectedProduct?.let { product ->
                    ProfitCard(
                        profit = profitText,
                        salePrice = form.salePrice,
                        cost = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                            .format(product.cost.multiply(java.math.BigDecimal.valueOf(form.quantity.toLong())))
                    )
                }

                if (form.isPaymentSelectionComplete && !state.isLoadingTariffs) {
                    Text(
                        text = state.tariffAmount?.let { amount ->
                            stringResource(
                                R.string.purchase_tariff_applied,
                                state.appliedTariff.rateBps.bpsToRate().toPlainString(),
                                currencyFormat.format(amount)
                            )
                        } ?: stringResource(R.string.purchase_tariff_none),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (state.isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SaveProductButtonContent(
                            label = stringResource(R.string.purchase_delete_submit),
                            enabled = !state.isSaving,
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Delete,
                            containerColor = MaterialTheme.colorScheme.error,
                            testTag = DELETE_PRODUCT_BUTTON_TAG
                        )
                        SaveProductButtonContent(
                            label = stringResource(R.string.purchase_edit_submit),
                            loadingLabel = stringResource(R.string.purchase_edit_loading),
                            enabled = state.canSubmit,
                            isLoading = state.isSaving,
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Edit
                        )
                    }
                } else {
                    SaveProductButtonContent(
                        label = stringResource(R.string.purchase_create_submit),
                        loadingLabel = stringResource(R.string.purchase_create_loading),
                        enabled = state.canSubmit,
                        isLoading = state.isSaving,
                        onClick = onSave
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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
            snackbar = { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = ForestGreenHeader,
                    contentColor = Color.White
                )
            }
        )
        }
    }
}

@Composable
private fun ProfitCard(
    profit: String?,
    salePrice: String,
    cost: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.purchase_profit_title),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = profit ?: stringResource(R.string.purchase_profit_placeholder),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.purchase_profit_calculation, salePrice, cost),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPurchaseScreenPreview() {
    LucroVarejoTheme {
        AddPurchaseScreenContent(state = AddPurchaseUiState(isLoadingProducts = false))
    }
}

@Preview(showBackground = true, heightDp = 1000, name = "Editar venda")
@Composable
private fun EditPurchaseScreenPreview() {
    LucroVarejoTheme {
        AddPurchaseScreenContent(state = editPreviewState())
    }
}

@Preview(showBackground = true, heightDp = 1000, name = "Confirmar exclusão da venda")
@Composable
private fun DeletePurchaseDialogPreview() {
    LucroVarejoTheme {
        AddPurchaseScreenContent(state = editPreviewState().copy(showDeleteDialog = true))
    }
}

private fun editPreviewState() = AddPurchaseUiState(
    isLoadingProducts = false,
    key = "1",
    form = PurchaseFormState(
        selectedProduct = ProductModel(
            cod = "001",
            name = "Bolsa Feminina",
            cost = java.math.BigDecimal("50.00"),
            description = ""
        ),
        salePrice = "89.90",
        quantity = 2,
        paymentMethod = PaymentMethods.PIX
    )
)
