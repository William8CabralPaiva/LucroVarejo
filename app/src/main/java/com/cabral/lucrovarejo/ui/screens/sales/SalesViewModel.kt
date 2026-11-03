package com.cabral.lucrovarejo.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.usecase.GetSalesPageUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.ObserveSalesChangesUseCase
import com.cabral.lucrovarejo.ui.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModel @Inject constructor(
    private val getSalesPage: GetSalesPageUseCase,
    private val observeSalesChanges: ObserveSalesChangesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private var cursor: SalesCursor? = null
    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private val selectedFilter = MutableStateFlow(SalesFilter())

    init {
        observeSales()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS.milliseconds)
            selectedFilter.value = currentFilter()
        }
    }

    // O DatePicker devolve a data à meia-noite UTC; null remove o filtro.
    fun onDateSelected(utcMillis: Long?) {
        _uiState.update { it.copy(selectedDateMillis = utcMillis) }
        searchJob?.cancel()
        selectedFilter.value = currentFilter()
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

    private fun observeSales() {
        selectedFilter
            .flatMapLatest { filter ->
                observeSalesChanges(filter)
                    .onEach { reload(filter) }
            }
            .catch {
                _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, hasError = true)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun reload(filter: SalesFilter = currentFilter()) {
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
        loadPage(filter)
    }

    private fun loadPage(filter: SalesFilter = currentFilter()) {
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

        val (start, end) = DateUtils.localDayRangeFromUtcMillis(utcMillis)
        return SalesFilter(nameQuery = state.query, dateStart = start, dateEnd = end)
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
