package com.cabral.lucrovarejo.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.usecase.GetLatestPurchasesUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.GetMonthlyPurchaseSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val getMonthlySummary: GetMonthlyPurchaseSummaryUseCase,
    private val getLatestPurchases: GetLatestPurchasesUseCase
) : ViewModel() {
    private val initialCalendar = Calendar.getInstance()
    private val _uiState = MutableStateFlow(
        HomeUiState(
            year = initialCalendar.get(Calendar.YEAR),
            month = initialCalendar.get(Calendar.MONTH) + 1
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val selectedMonth = MutableStateFlow(_uiState.value.year to _uiState.value.month)

    init {
        observeSummary()
        observeLatestPurchases()
    }

    fun onMonthYearChanged(year: Int, month: Int) {
        require(year > 0)
        require(month in 1..12)
        _uiState.update { it.copy(year = year, month = month) }
        selectedMonth.value = year to month
    }

    // Observa o Firestore em tempo real: ao cadastrar uma venda, o snapshot listener emite os
    // dados novos e o Home atualiza sozinho, sem refresh manual nem ActivityResult.
    private fun observeSummary() {
        selectedMonth
            .flatMapLatest { (year, month) ->
                val (start, end) = monthRange(year, month)
                getMonthlySummary(start, end)
                    .onStart {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    .onEach { summary ->
                        _uiState.update {
                            it.copy(summary = summary, isLoading = false, errorMessage = null)
                        }
                    }
                    .catch { failure ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = (failure as? PurchaseFailure)
                                    ?.let { purchaseErrorMessage(it.reason) }
                                    ?: R.string.summary_load_error_generic
                            )
                        }
                    }
            }
            .launchIn(viewModelScope)
    }

    // Mesmo mecanismo do resumo: novas vendas aparecem aqui assim que o Firestore as emite.
    private fun observeLatestPurchases() {
        getLatestPurchases(LATEST_PURCHASES_LIMIT)
            .onEach { purchases ->
                _uiState.update {
                    it.copy(
                        latestPurchases = purchases,
                        isLatestPurchasesLoading = false,
                        hasLatestPurchasesError = false
                    )
                }
            }
            .catch {
                _uiState.update {
                    it.copy(isLatestPurchasesLoading = false, hasLatestPurchasesError = true)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun purchaseErrorMessage(reason: PurchaseFailure.Reason): Int = when (reason) {
        PurchaseFailure.Reason.NETWORK -> R.string.summary_load_error_network
        PurchaseFailure.Reason.PERMISSION_DENIED -> R.string.summary_load_error_permission
        PurchaseFailure.Reason.UNAUTHENTICATED -> R.string.summary_load_error_unauthenticated
        PurchaseFailure.Reason.UNKNOWN -> R.string.summary_load_error_generic
    }

    private fun monthRange(year: Int, month: Int): Pair<Date, Date> {
        val start = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = start.clone() as Calendar
        end.add(Calendar.MONTH, 1)
        return start.time to end.time
    }

    private companion object {
        const val LATEST_PURCHASES_LIMIT = 4
    }
}