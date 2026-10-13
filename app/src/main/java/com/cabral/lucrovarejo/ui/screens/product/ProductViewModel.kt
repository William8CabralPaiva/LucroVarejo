package com.cabral.lucrovarejo.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductCreationFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.usecase.CreateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

sealed interface ProductEffect {
    data object ProductCreated : ProductEffect
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val createProduct: CreateProductUseCase
) : ViewModel() {
    private val effectChannel = Channel<ProductEffect>(Channel.BUFFERED)
    val effects: Flow<ProductEffect> = effectChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _uiState.update {
            it.copy(name = value, nameError = null, errorMessage = null)
        }
    }

    fun onCodeChanged(value: String) {
        _uiState.update { it.copy(cod = value, errorMessage = null) }
    }

    fun onCostChanged(value: String) {
        _uiState.update {
            it.copy(cost = value, costError = null, errorMessage = null)
        }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update {
            it.copy(description = value, errorMessage = null)
        }
    }

    fun saveProduct() {
        val current = _uiState.value
        if (current.isLoading) return

        val nameError = if (current.name.isBlank()) R.string.product_name_required else null
        val costError = when {
            current.cost.isBlank() -> R.string.product_cost_required
            parseProductCost(current.cost) == null -> R.string.product_cost_invalid
            else -> null
        }
        if (nameError != null || costError != null) {
            _uiState.update {
                it.copy(nameError = nameError, costError = costError, errorMessage = null)
            }
            return
        }

        val product = ProductModel(
            cod = current.cod.trim(),
            name = current.name.trim(),
            cost = requireNotNull(parseProductCost(current.cost)),
            description = current.description.trim()
        )
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        viewModelScope.launch {
            try {
                createProduct(product)
                _uiState.update {
                    it.copy(
                        name = "",
                        cod = "",
                        cost = "",
                        description = "",
                        nameError = null,
                        costError = null,
                        isLoading = false
                    )
                }
                effectChannel.send(ProductEffect.ProductCreated)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: ProductCreationFailure) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = productErrorMessage(failure.reason))
                }
            }
        }
    }

    private fun productErrorMessage(reason: ProductCreationFailure.Reason): Int =
        when (reason) {
            ProductCreationFailure.Reason.NETWORK -> R.string.product_create_error_network
            ProductCreationFailure.Reason.PERMISSION_DENIED ->
                R.string.product_create_error_permission
            ProductCreationFailure.Reason.UNAUTHENTICATED ->
                R.string.product_create_error_unauthenticated
            ProductCreationFailure.Reason.UNKNOWN -> R.string.product_create_error_generic
        }
}
