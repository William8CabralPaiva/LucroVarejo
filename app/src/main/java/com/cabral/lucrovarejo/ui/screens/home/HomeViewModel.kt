package com.cabral.lucrovarejo.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.usecase.GetMonthlyPurchaseSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.Date
import java.util.concurrent.CancellationException
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMonthlySummary: GetMonthlyPurchaseSummaryUseCase
) : ViewModel() {
    private val initialCalendar = Calendar.getInstance()
    private val _uiState = MutableStateFlow(
        HomeUiState(
            year = initialCalendar.get(Calendar.YEAR),
            month = initialCalendar.get(Calendar.MONTH) + 1
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var summaryJob: Job? = null

    init {
        loadSummary(_uiState.value.year, _uiState.value.month)
    }

    fun onMonthYearChanged(year: Int, month: Int) {
        require(year > 0)
        require(month in 1..12)
        loadSummary(year, month)
    }

    private fun loadSummary(year: Int, month: Int) {
        summaryJob?.cancel()
        _uiState.update {
            it.copy(year = year, month = month, isLoading = true, errorMessage = null)
        }
        val (start, end) = monthRange(year, month)
        summaryJob = viewModelScope.launch {
            try {
                val summary = getMonthlySummary(start, end)
                _uiState.update {
                    it.copy(summary = summary, isLoading = false, errorMessage = null)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: PurchaseFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = purchaseErrorMessage(failure.reason)
                    )
                }
            } catch (failure: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = R.string.summary_load_error_generic
                    )
                }
            }
        }
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
}
