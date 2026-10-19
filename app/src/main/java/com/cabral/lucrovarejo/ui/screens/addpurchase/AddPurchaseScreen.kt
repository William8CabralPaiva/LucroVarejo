package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.MaterialTheme
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AddPurchaseScreen(
    onPurchaseSaved: () -> Unit = {},
    viewModel: AddPurchaseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.purchase_create_success)
    LaunchedEffect(viewModel, successMessage) {
        viewModel.effects.collect {
            onPurchaseSaved()
            snackbarHostState.showSnackbar(successMessage)
        }
    }
    AddPurchaseScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onProductSelected = viewModel::onProductSelected,
        onSalePriceChanged = viewModel::onSalePriceChanged,
        onQuantityChanged = viewModel::onQuantityChanged,
        onPaymentMethodSelected = viewModel::onPaymentMethodSelected,
        onSaleDateChanged = viewModel::onSaleDateChanged,
        onSave = viewModel::savePurchase
    )
}

@Composable
fun AddPurchaseScreenContent(
    state: AddPurchaseUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onProductSelected: (String) -> Unit = {},
    onSalePriceChanged: (String) -> Unit = {},
    onQuantityChanged: (Int) -> Unit = {},
    onPaymentMethodSelected: (PaymentMethods) -> Unit = {},
    onSaleDateChanged: (Long) -> Unit = {},
    onSave: () -> Unit = {}
) {
    val paymentMethods = PaymentMethodsUi.entries
    val paymentLabels = paymentMethods.map { it.displayName }
    val form = state.form
    val selectedPaymentLabel = paymentMethods
        .firstOrNull { it.method == form.paymentMethod }
        ?.displayName
    val salePriceError = form.salePriceError?.let { stringResource(it) }
    val errorMessage = state.errorMessage?.let { stringResource(it) }
    val profitText = state.profit?.let { profit ->
        remember(profit) {
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(profit)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
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
                SpinnerContent(
                    selectedItem = form.selectedProduct?.let(::productDisplayName),
                    items = state.products.map(::productDisplayName),
                    onItemSelected = onProductSelected,
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
                    onValueChange = onSalePriceChanged,
                    labelResId = R.string.purchase_sale_price_label,
                    placeholderResId = R.string.product_paid_value_placeholder,
                    icon = Icons.Outlined.MonetizationOn,
                    type = IconTextFieldType.MONEY,
                    required = true,
                    enabled = !state.isSaving,
                    isError = form.salePriceError != null,
                    supportingText = salePriceError?.let { message ->
                        { Text(message) }
                    }
                )

                QuantityFieldContent(
                    value = form.quantity,
                    onValueChange = onQuantityChanged
                )

                SpinnerContent(
                    selectedItem = selectedPaymentLabel,
                    items = paymentLabels,
                    onItemSelected = { selectedLabel ->
                        paymentMethods.firstOrNull { it.displayName == selectedLabel }
                            ?.method
                            ?.let(onPaymentMethodSelected)
                    },
                    textId = R.string.purchase_payment_method_label,
                    placeholderResId = R.string.purchase_payment_method_placeholder,
                    required = true,
                    enabled = !state.isSaving
                )

                DateTextField(
                    initialDateMillis = form.saleDateMillis,
                    onDateChange = onSaleDateChanged
                )

                form.selectedProduct?.let { product ->
                    ProfitCard(
                        profit = profitText,
                        salePrice = form.salePrice,
                        cost = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                            .format(product.cost.multiply(java.math.BigDecimal.valueOf(form.quantity.toLong())))
                    )
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SaveProductButtonContent(
                    label = stringResource(R.string.purchase_create_submit),
                    loadingLabel = stringResource(R.string.purchase_create_loading),
                    enabled = state.canSubmit,
                    isLoading = state.isSaving,
                    onClick = onSave
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
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
fun AddPurchaseScreenPreview() {
    LucroVarejoTheme {
        AddPurchaseScreenContent(state = AddPurchaseUiState(isLoadingProducts = false))
    }
}
