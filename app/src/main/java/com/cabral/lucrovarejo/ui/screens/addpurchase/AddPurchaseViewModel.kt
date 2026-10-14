package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.usecase.CreatePurchaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.RoundingMode
import java.util.Date
import java.util.concurrent.CancellationException
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddPurchaseViewModel @Inject constructor(
    private val getProducts: GetProductsUseCase,
    private val createPurchase: CreatePurchaseUseCase
) : ViewModel() {
    private val effectChannel = Channel<Unit>(Channel.BUFFERED)
    val effects: Flow<Unit> = effectChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow(AddPurchaseUiState())
    val uiState: StateFlow<AddPurchaseUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun onProductSelected(displayName: String) {
        val product = _uiState.value.products.firstOrNull {
            productDisplayName(it) == displayName
        } ?: return
        _uiState.update {
            it.copy(form = it.form.copy(selectedProduct = product), errorMessage = null)
        }
    }

    fun onSalePriceChanged(value: String) {
        _uiState.update {
            it.copy(
                form = it.form.copy(salePrice = value, salePriceError = null),
                errorMessage = null
            )
        }
    }

    fun onQuantityChanged(value: Int) {
        _uiState.update {
            it.copy(form = it.form.copy(quantity = value.coerceIn(1, 100)), errorMessage = null)
        }
    }

    fun onPaymentMethodSelected(value: PaymentMethods) {
        _uiState.update {
            it.copy(form = it.form.copy(paymentMethod = value), errorMessage = null)
        }
    }

    fun onSaleDateChanged(value: Long) {
        _uiState.update {
            it.copy(form = it.form.copy(saleDateMillis = value), errorMessage = null)
        }
    }

    fun savePurchase() {
        val current = _uiState.value
        if (current.isSaving || current.isLoadingProducts) return

        val form = current.form
        val salePrice = parseSalePrice(form.salePrice)
        val priceError = if (form.salePrice.isBlank()) {
            R.string.purchase_sale_price_required
        } else if (salePrice == null) {
            R.string.purchase_sale_price_invalid
        } else {
            null
        }
        if (form.selectedProduct == null || form.paymentMethod == null || priceError != null) {
            _uiState.update {
                it.copy(form = it.form.copy(salePriceError = priceError))
            }
            return
        }

        val product = form.selectedProduct
        val purchase = PurchaseModel(
            cod = product.cod,
            name = product.name,
            cost = product.cost
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .intValueExact(),
            description = product.description,
            salePrice = requireNotNull(salePrice)
                .movePointRight(2)
                .intValueExact(),
            quantity = form.quantity,
            saleDate = Date(form.saleDateMillis),
            paymentMethods = listOf(form.paymentMethod)
        )
        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null,
                form = it.form.copy(salePriceError = null)
            )
        }
        viewModelScope.launch {
            try {
                createPurchase(purchase)
                _uiState.update { it.copy(isSaving = false) }
                effectChannel.send(Unit)
            } catch (failure: PurchaseFailure) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = purchaseErrorMessage(failure.reason))
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                val products = getProducts()
                _uiState.update {
                    it.copy(products = products, isLoadingProducts = false, errorMessage = null)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: ProductFailure) {
                _uiState.update {
                    it.copy(
                        isLoadingProducts = false,
                        errorMessage = productErrorMessage(failure.reason)
                    )
                }
            } catch (failure: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingProducts = false,
                        errorMessage = R.string.purchase_load_products_error_generic
                    )
                }
            }
        }
    }

    private fun productErrorMessage(reason: ProductFailure.Reason): Int = when (reason) {
        ProductFailure.Reason.NETWORK -> R.string.purchase_load_products_error_network
        ProductFailure.Reason.PERMISSION_DENIED -> R.string.purchase_load_products_error_permission
        ProductFailure.Reason.UNAUTHENTICATED -> R.string.purchase_load_products_error_unauthenticated
        ProductFailure.Reason.UNKNOWN -> R.string.purchase_load_products_error_generic
    }

    private fun purchaseErrorMessage(reason: PurchaseFailure.Reason): Int = when (reason) {
        PurchaseFailure.Reason.NETWORK -> R.string.purchase_create_error_network
        PurchaseFailure.Reason.PERMISSION_DENIED -> R.string.purchase_create_error_permission
        PurchaseFailure.Reason.UNAUTHENTICATED -> R.string.purchase_create_error_unauthenticated
        PurchaseFailure.Reason.UNKNOWN -> R.string.purchase_create_error_generic
    }
}

internal fun productDisplayName(product: ProductModel): String =
    if (product.cod.isBlank()) product.name else "${product.name} (${product.cod})"
