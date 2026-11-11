package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.common.DataFailure
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.usecase.GetSaleTariffUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetTerminalTariffsUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetTerminalsUseCase
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.usecase.CreatePurchaseUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.DeletePurchaseUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.UpdatePurchaseUseCase
import com.cabral.lucrovarejo.ui.utils.DateUtils
import com.cabral.lucrovarejo.ui.utils.userMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import javax.inject.Inject

private const val MAX_QUANTITY = 100

@HiltViewModel
class AddPurchaseViewModel @Inject constructor(
    private val getProducts: GetProductsUseCase,
    private val createPurchase: CreatePurchaseUseCase,
    private val updatePurchase: UpdatePurchaseUseCase,
    private val deletePurchase: DeletePurchaseUseCase,
    private val getTerminals: GetTerminalsUseCase,
    private val getTerminalTariffs: GetTerminalTariffsUseCase,
    private val getSaleTariff: GetSaleTariffUseCase,
) : ViewModel() {
    private val effectChannel = Channel<Unit>(Channel.BUFFERED)
    val effects: Flow<Unit> = effectChannel.receiveAsFlow()

    // Emitido apos editar/deletar com sucesso: a tela deve voltar para a lista de vendas.
    private val completionChannel = Channel<Unit>(Channel.BUFFERED)
    val completions: Flow<Unit> = completionChannel.receiveAsFlow()

    private val _uiState = MutableStateFlow(AddPurchaseUiState())
    val uiState: StateFlow<AddPurchaseUiState> = _uiState.asStateFlow()

    // Tarifas ficam em memória só durante a vida da tela; o Firestore já faz o cache offline.
    private val tariffsByTerminal = mutableMapOf<String, List<PaymentMachineModel>>()
    private var tariffsJob: Job? = null

    init {
        observeProducts()
        observeTerminals()
    }

    fun loadPurchase(purchase: PurchaseModel?) {
        if (purchase?.key == null || _uiState.value.key == purchase.key) return
        _uiState.update {
            it.copy(
                key = purchase.key,
                errorMessage = null,
                form = PurchaseFormState(
                    selectedProduct = ProductModel(
                        cod = purchase.cod,
                        name = purchase.name,
                        cost = BigDecimal.valueOf(purchase.cost.toLong(), 2),
                        description = purchase.description
                    ),
                    salePrice = BigDecimal.valueOf(purchase.salePrice.toLong(), 2).toPlainString(),
                    quantity = purchase.quantity.coerceIn(1, MAX_QUANTITY),
                    paymentMethod = purchase.paymentMethod,
                    terminalId = purchase.terminalId,
                    terminalName = purchase.terminalName,
                    brand = purchase.brand,
                    installments = purchase.installments,
                    saleDateMillis = purchase.saleDate.time
                ),
                appliedTariff = AppliedTariff(purchase.tariffId, purchase.tariffRateBps),
                keepSavedTariff = true
            )
        }
        loadTariffsForSelection()
    }

    fun onProductSelected(displayName: String) {
        val product = _uiState.value.products.firstOrNull {
            productDisplayName(it) == displayName
        } ?: return
        selectProduct(product)
    }

    fun onProductKeySelected(optionKey: String) {
        val product = _uiState.value.products.firstOrNull {
            productOptionKey(it) == optionKey
        } ?: return
        selectProduct(product)
    }

    private fun selectProduct(product: ProductModel) {
        _uiState.update {
            it.copy(form = it.form.copy(selectedProduct = product), errorMessage = null)
        }
    }

    fun onProductTextChanged(text: String) {
        _uiState.update { state ->
            val selected = state.form.selectedProduct ?: return@update state
            if (productDisplayName(selected) == text) state
            else state.copy(form = state.form.copy(selectedProduct = null))
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
            it.copy(form = it.form.copy(quantity = value.coerceIn(1, MAX_QUANTITY)), errorMessage = null)
        }
    }

    fun onPaymentMethodSelected(value: PaymentMethods) {
        if (_uiState.value.form.paymentMethod == value) return
        _uiState.update {
            it.copy(
                form = it.form.copy(
                    paymentMethod = value,
                    terminalId = null,
                    terminalName = null,
                    brand = null,
                    installments = null
                ),
                tariffs = emptyList(),
                appliedTariff = AppliedTariff.NONE,
                keepSavedTariff = false,
                tariffNotice = null,
                errorMessage = null
            )
        }
        loadTariffsForSelection()
        selectSingleTerminal()
    }

    // Com uma única maquininha ativa para o método, não há o que escolher.
    private fun selectSingleTerminal() {
        val state = _uiState.value
        if (state.form.terminalId != null) return
        state.terminalOptions.singleOrNull()?.let { onTerminalSelected(it.name) }
    }

    fun onTerminalSelected(name: String) {
        val terminal = _uiState.value.terminalOptions.firstOrNull { it.name == name } ?: return
        val terminalKey = terminal.key ?: return
        _uiState.update {
            it.copy(
                form = it.form.copy(
                    terminalId = terminalKey,
                    terminalName = terminal.name,
                    brand = null,
                    installments = null
                ),
                tariffs = emptyList(),
                appliedTariff = AppliedTariff.NONE,
                keepSavedTariff = false,
                tariffNotice = null,
                errorMessage = null
            )
        }
        loadTariffs(listOf(terminalKey))
    }

    fun onBrandSelected(brand: String) {
        if (brand !in _uiState.value.brandOptions) return
        _uiState.update {
            reconcileTariff(
                it.copy(
                    form = it.form.copy(brand = brand, installments = null),
                    keepSavedTariff = false,
                    tariffNotice = null,
                    errorMessage = null
                )
            )
        }
    }

    fun onInstallmentsSelected(installments: Int) {
        if (installments !in _uiState.value.installmentOptions) return
        _uiState.update {
            reconcileTariff(
                it.copy(
                    form = it.form.copy(installments = installments),
                    keepSavedTariff = false,
                    tariffNotice = null,
                    errorMessage = null
                )
            )
        }
    }

    fun onSaleDateChanged(value: Long) {
        _uiState.update {
            reconcileTariff(
                it.copy(
                    form = it.form.copy(
                        saleDateMillis = DateUtils.mergeDateWithExistingTime(
                            dateMillis = value,
                            existingDateTimeMillis = it.form.saleDateMillis
                        )
                    ),
                    keepSavedTariff = false,
                    tariffNotice = null,
                    errorMessage = null
                )
            )
        }
    }

    fun savePurchase() {
        val current = _uiState.value
        if (current.isSaving || current.isLoadingProducts || current.isLoadingTariffs) return
        if (current.hasLoss) return

        val form = current.form
        val salePrice = parseSalePrice(form.salePrice)
        val priceError = if (form.salePrice.isBlank()) {
            R.string.purchase_sale_price_required
        } else if (salePrice == null) {
            R.string.purchase_sale_price_invalid
        } else {
            null
        }
        if (form.selectedProduct == null || !form.isPaymentSelectionComplete || priceError != null) {
            _uiState.update {
                it.copy(form = it.form.copy(salePriceError = priceError))
            }
            return
        }

        val purchase = current.toPurchase(form.selectedProduct, requireNotNull(salePrice))
        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null,
                form = it.form.copy(salePriceError = null)
            )
        }
        viewModelScope.launch {
            try {
                if (purchase.key != null) {
                    updatePurchase(purchase)
                    _uiState.update { it.copy(isSaving = false) }
                    completionChannel.send(Unit)
                    return@launch
                }
                createPurchase(purchase)
                _uiState.update { it.resetAfterCreate() }
                effectChannel.send(Unit)
            } catch (failure: DataFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = failure.userMessageRes(
                            if (purchase.key != null) R.string.purchase_edit_error_generic
                            else R.string.purchase_create_error_generic
                        )
                    )
                }
            }
        }
    }

    fun onDeleteRequested() {
        if (_uiState.value.key == null || _uiState.value.isSaving) return
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        val current = _uiState.value
        val key = current.key ?: return
        if (current.isSaving) return

        _uiState.update { it.copy(showDeleteDialog = false, isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                deletePurchase(key)
                _uiState.update { it.copy(isSaving = false) }
                completionChannel.send(Unit)
            } catch (failure: DataFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = failure.userMessageRes(R.string.purchase_delete_error_generic)
                    )
                }
            }
        }
    }

    private fun observeProducts() {
        getProducts()
            .onEach { products ->
                _uiState.update {
                    it.copy(products = products, isLoadingProducts = false, errorMessage = null)
                }
            }
            .catch { failure ->
                _uiState.update {
                    it.copy(
                        isLoadingProducts = false,
                        errorMessage = failure.userMessageRes(R.string.purchase_load_products_error_generic)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTerminals() {
        getTerminals()
            .onEach { terminals ->
                _uiState.update { current ->
                    val terminalName = terminals
                        .firstOrNull { it.key != null && it.key == current.form.terminalId }
                        ?.name
                    current.copy(
                        terminals = terminals,
                        form = if (terminalName != null) {
                            current.form.copy(terminalName = terminalName)
                        } else {
                            current.form
                        }
                    )
                }
                val state = _uiState.value
                if (state.form.paymentMethod == PaymentMethods.CASH &&
                    state.tariffs.isEmpty() && !state.isLoadingTariffs
                ) {
                    loadTariffsForSelection()
                }
                if (!_uiState.value.isEditing) selectSingleTerminal()
            }
            .catch {
                _uiState.update { it.copy(errorMessage = R.string.purchase_terminals_load_error) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadTariffsForSelection() {
        val state = _uiState.value
        when {
            state.form.paymentMethod == PaymentMethods.CASH -> loadTariffs(
                state.terminals
                    .filter { it.active && TerminalPaymentType.CASH in it.enabledMethods }
                    .mapNotNull { it.key }
            )

            state.form.terminalId != null -> loadTariffs(listOf(state.form.terminalId))
        }
    }

    private fun loadTariffs(terminalIds: List<String>) {
        tariffsJob?.cancel()
        if (terminalIds.isEmpty()) {
            _uiState.update { reconcileTariff(it.copy(tariffs = emptyList(), isLoadingTariffs = false)) }
            return
        }
        _uiState.update { it.copy(tariffs = emptyList(), isLoadingTariffs = true) }
        tariffsJob = viewModelScope.launch {
            try {
                val loaded = terminalIds.flatMap { id ->
                    tariffsByTerminal[id] ?: getTerminalTariffs(id).also { tariffsByTerminal[id] = it }
                }
                // Em edição, a primeira carga substitui o snapshot salvo pela tarifa vigente atual.
                _uiState.update {
                    reconcileTariff(
                        it.copy(tariffs = loaded, isLoadingTariffs = false, keepSavedTariff = false)
                    )
                }
            } catch (_: DataFailure) {
                _uiState.update {
                    it.copy(
                        tariffs = emptyList(),
                        isLoadingTariffs = false,
                        errorMessage = R.string.purchase_tariffs_load_error
                    )
                }
            }
        }
    }

    // Revalida a seleção contra as tarifas vigentes na data da venda e recalcula a tarifa aplicada.
    private fun reconcileTariff(state: AddPurchaseUiState): AddPurchaseUiState {
        if (state.keepSavedTariff || state.isLoadingTariffs) return state
        var form = state.form
        var notice = state.tariffNotice
        if (state.showBrand && form.terminalId != null) {
            if (form.brand != null && form.brand !in state.brandOptions) {
                form = form.copy(brand = null, installments = null)
                notice = R.string.purchase_tariff_selection_cleared
            } else if (form.installments != null && form.installments !in state.installmentOptions) {
                form = form.copy(installments = null)
                notice = R.string.purchase_tariff_selection_cleared
            }
        }
        val updated = state.copy(form = form, tariffNotice = notice)
        return updated.copy(appliedTariff = resolveTariff(updated))
    }

    private fun resolveTariff(state: AddPurchaseUiState): AppliedTariff {
        val form = state.form
        val method = form.paymentMethod
        if (method == null || !form.isPaymentSelectionComplete) return AppliedTariff.NONE
        val tariff = getSaleTariff(
            tariffs = state.tariffs,
            method = method,
            brandId = form.brand.takeIf { state.showBrand },
            installments = form.installments.takeIf { state.showInstallments },
            saleDate = Date(form.saleDateMillis)
        )
        return tariff?.let { AppliedTariff(it.key, it.rateBps) } ?: AppliedTariff.NONE
    }
}

internal fun productOptionKey(product: ProductModel): String =
    product.key ?: productDisplayName(product)

internal fun productDisplayName(product: ProductModel): String = product.name

private fun AddPurchaseUiState.toPurchase(product: ProductModel, salePrice: BigDecimal) = PurchaseModel(
    key = key,
    cod = product.cod,
    name = product.name,
    cost = product.cost
        .setScale(2, RoundingMode.UNNECESSARY)
        .movePointRight(2)
        .intValueExact(),
    description = product.description,
    salePrice = salePrice.movePointRight(2).intValueExact(),
    quantity = form.quantity,
    saleDate = Date(form.saleDateMillis),
    paymentMethod = requireNotNull(form.paymentMethod),
    terminalId = form.terminalId,
    terminalName = form.terminalName,
    brand = form.brand,
    installments = form.installments,
    tariffRateBps = appliedTariff.rateBps,
    tariffId = appliedTariff.tariffId,
    tariffAmountCents = tariffAmountCents()
)

private fun AddPurchaseUiState.resetAfterCreate() = copy(
    isSaving = false,
    form = PurchaseFormState(),
    tariffs = emptyList(),
    appliedTariff = AppliedTariff.NONE,
    keepSavedTariff = false,
    tariffNotice = null,
    formResetCount = formResetCount + 1
)
