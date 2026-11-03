package com.cabral.lucrovarejo.ui.screens.addproduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.usecase.CreateProductUseCase
import com.cabral.lucrovarejo.domain.product.usecase.DeleteProductUseCase
import com.cabral.lucrovarejo.domain.product.usecase.UpdateProductUseCase
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

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val createProduct: CreateProductUseCase,
    private val updateProduct: UpdateProductUseCase,
    private val deleteProduct: DeleteProductUseCase
) : ViewModel() {
    private val effectChannel = Channel<Unit>(Channel.BUFFERED)
    val effects: Flow<Unit> = effectChannel.receiveAsFlow()

    // Emitido apos editar/deletar com sucesso: a tela deve voltar para a lista.
    private val completionChannel = Channel<Unit>(Channel.BUFFERED)
    val completions: Flow<Unit> = completionChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    fun loadProduct(product: ProductModel?) {
        if (product?.key == null || _uiState.value.key == product.key) return
        _uiState.update {
            AddProductUiState(
                key = product.key,
                name = product.name,
                cod = product.cod,
                cost = product.cost.toPlainString(),
                description = product.description
            )
        }
    }

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
            key = current.key,
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
                if (product.key != null) {
                    updateProduct(product)
                    _uiState.update { it.copy(isLoading = false) }
                    completionChannel.send(Unit)
                    return@launch
                }
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
                effectChannel.send(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: ProductFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = productErrorMessage(
                            failure.reason,
                            R.string.product_edit_error_generic.takeIf { product.key != null }
                        )
                    )
                }
            }
        }
    }

    fun onDeleteRequested() {
        if (_uiState.value.key == null || _uiState.value.isLoading) return
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        val current = _uiState.value
        val key = current.key ?: return
        if (current.isLoading) return

        _uiState.update { it.copy(showDeleteDialog = false, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                deleteProduct(key)
                _uiState.update { it.copy(isLoading = false) }
                completionChannel.send(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: ProductFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = productErrorMessage(
                            failure.reason,
                            R.string.product_delete_error_generic
                        )
                    )
                }
            }
        }
    }

    private fun productErrorMessage(reason: ProductFailure.Reason, unknownOverride: Int? = null): Int =
        when (reason) {
            ProductFailure.Reason.NETWORK -> R.string.product_create_error_network
            ProductFailure.Reason.PERMISSION_DENIED ->
                R.string.product_create_error_permission
            ProductFailure.Reason.UNAUTHENTICATED ->
                R.string.product_create_error_unauthenticated
            ProductFailure.Reason.UNKNOWN ->
                unknownOverride ?: R.string.product_create_error_generic
        }
}
