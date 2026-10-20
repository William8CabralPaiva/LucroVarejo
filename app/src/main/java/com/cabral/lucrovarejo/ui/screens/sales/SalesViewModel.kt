package com.cabral.lucrovarejo.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.usecase.GetSalesPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val getSalesPage: GetSalesPageUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private var cursor: SalesCursor? = null
    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        reload()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS.milliseconds)
            reload()
        }
    }

    // O DatePicker devolve a data à meia-noite UTC; null remove o filtro.
    fun onDateSelected(utcMillis: Long?) {
        _uiState.update { it.copy(selectedDateMillis = utcMillis) }
        searchJob?.cancel()
        reload()
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.endReached || state.hasError) return
        _uiState.update { it.copy(isLoadingMore = true) }
        loadPage()
    }

    fun retry() {
        if (_uiState.value.items.isEmpty()) {
            reload()
        } else {
            _uiState.update { it.copy(hasError = false, isLoadingMore = true) }
            loadPage()
        }
    }

    private fun reload() {
        loadJob?.cancel()
        cursor = null
        _uiState.update {
            it.copy(
                items = emptyList(),
                isLoading = true,
                isLoadingMore = false,
                endReached = false,
                hasError = false
            )
        }
        loadPage()
    }

    private fun loadPage() {
        val filter = currentFilter()
        loadJob = viewModelScope.launch {
            try {
                val page = getSalesPage(filter, cursor)
                ensureActive()
                cursor = page.nextCursor
                _uiState.update {
                    it.copy(
                        items = it.items + page.items,
                        isLoading = false,
                        isLoadingMore = false,
                        endReached = !page.hasMore,
                        hasError = false
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, hasError = true)
                }
            }
        }
    }

    private fun currentFilter(): SalesFilter {
        val state = _uiState.value
        val utcMillis = state.selectedDateMillis ?: return SalesFilter(nameQuery = state.query)

        val utcDay = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        val start = Calendar.getInstance().apply {
            clear()
            set(
                utcDay.get(Calendar.YEAR),
                utcDay.get(Calendar.MONTH),
                utcDay.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        return SalesFilter(nameQuery = state.query, dateStart = start.time, dateEnd = end.time)
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
