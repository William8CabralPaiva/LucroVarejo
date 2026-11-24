package com.cabral.lucrovarejo.ui.store

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.usecase.GetStoreDisplayNameUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.UpdateStoreDisplayNameUseCase
import com.cabral.lucrovarejo.domain.store.StoreRepository
import com.cabral.lucrovarejo.domain.store.StoreSelection
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.cabral.lucrovarejo.domain.store.usecase.CreateStoreUseCase
import com.cabral.lucrovarejo.domain.store.usecase.GetStoreSelectionUseCase
import com.cabral.lucrovarejo.domain.store.usecase.SwitchStoreUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class StoreViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val storeA = StoreSummary("a", "Loja A")
    private val storeB = StoreSummary("b", "")
    private lateinit var repository: FakeStoreRepository
    private lateinit var auth: FakeAuth

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeStoreRepository(StoreSelection(listOf(storeA, storeB), "a"))
        auth = FakeAuth()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = StoreViewModel(
        GetStoreDisplayNameUseCase(auth),
        UpdateStoreDisplayNameUseCase(auth),
        GetStoreSelectionUseCase(repository),
        CreateStoreUseCase(repository),
        SwitchStoreUseCase(repository)
    )

    @Test
    fun loadPopulatesNameAndStores() = runTest(dispatcher) {
        val vm = viewModel()
        vm.load()
        advanceUntilIdle()

        assertEquals("Loja A", vm.uiState.value.displayName)
        assertEquals(listOf(storeA, storeB), vm.uiState.value.stores)
        assertEquals("a", vm.uiState.value.activeStoreId)
    }

    @Test
    fun loadFailuresKeepDefaults() = runTest(dispatcher) {
        auth.nameFailure = true
        repository.selectionFailure = true
        val vm = viewModel()
        vm.load()
        advanceUntilIdle()

        assertEquals(StoreUiState(), vm.uiState.value)
    }

    @Test
    fun selectStoreSwitchesAndBumpsEpoch() = runTest(dispatcher) {
        val vm = viewModel()
        vm.load()
        advanceUntilIdle()

        vm.selectStore("b")

        assertEquals(listOf("b"), repository.selected)
        assertEquals("b", vm.uiState.value.activeStoreId)
        assertNull(vm.uiState.value.displayName)
        assertEquals(1, vm.uiState.value.storeEpoch)
    }

    @Test
    fun selectStoreIgnoresActiveOrUnknown() = runTest(dispatcher) {
        val vm = viewModel()
        vm.load()
        advanceUntilIdle()

        vm.selectStore("a")
        vm.selectStore("zzz")

        assertTrue(repository.selected.isEmpty())
        assertEquals(0, vm.uiState.value.storeEpoch)
    }

    @Test
    fun createStoreAddsAndActivates() = runTest(dispatcher) {
        val vm = viewModel()
        var result: Boolean? = null
        vm.createStore("  Nova  ") { result = it }
        advanceUntilIdle()

        assertEquals(true, result)
        assertEquals("Nova", vm.uiState.value.stores.single().displayName)
        assertEquals("new-id", vm.uiState.value.activeStoreId)
        assertEquals("Nova", vm.uiState.value.displayName)
        assertEquals(listOf("new-id"), repository.selected)
    }

    @Test
    fun createStoreFailureReportsFalse() = runTest(dispatcher) {
        repository.createFailure = true
        val vm = viewModel()
        var result: Boolean? = null
        vm.createStore("Nova") { result = it }
        advanceUntilIdle()

        assertEquals(false, result)
        assertTrue(vm.uiState.value.stores.isEmpty())
        assertTrue(repository.selected.isEmpty())
    }

    @Test
    fun updateDisplayNameUpdatesStateAndActiveStore() = runTest(dispatcher) {
        val vm = viewModel()
        vm.load()
        advanceUntilIdle()
        var result: Boolean? = null

        vm.updateDisplayName("  Renomeada ") { result = it }
        advanceUntilIdle()

        assertEquals(true, result)
        assertEquals("Renomeada", auth.updatedName)
        assertEquals("Renomeada", vm.uiState.value.displayName)
        assertEquals("Renomeada", vm.uiState.value.stores.first { it.id == "a" }.displayName)
        assertEquals("", vm.uiState.value.stores.first { it.id == "b" }.displayName)
    }

    @Test
    fun updateDisplayNameFailureKeepsState() = runTest(dispatcher) {
        val vm = viewModel()
        vm.load()
        advanceUntilIdle()
        auth.updateFailure = true
        var result: Boolean? = null

        vm.updateDisplayName("X") { result = it }
        advanceUntilIdle()

        assertEquals(false, result)
        assertEquals("Loja A", vm.uiState.value.displayName)
    }

    @Test
    fun tipsAndClear() = runTest(dispatcher) {
        val vm = viewModel()
        vm.showTips()
        assertTrue(vm.uiState.value.showTips)
        vm.dismissTips()
        assertFalse(vm.uiState.value.showTips)

        vm.load()
        advanceUntilIdle()
        vm.clear()
        assertEquals(StoreUiState(), vm.uiState.value)
    }

    private class FakeStoreRepository(private val selection: StoreSelection) : StoreRepository {
        val selected = mutableListOf<String>()
        var selectionFailure = false
        var createFailure = false

        override suspend fun getStoreSelection(): StoreSelection {
            if (selectionFailure) error("boom")
            return selection
        }

        override suspend fun createStore(displayName: String): StoreSummary {
            if (createFailure) error("boom")
            return StoreSummary("new-id", displayName)
        }

        override fun selectStore(storeId: String) {
            selected += storeId
        }
    }

    private class FakeAuth(
        delegate: AuthRepository = mock(AuthRepository::class.java)
    ) : AuthRepository by delegate {
        var nameFailure = false
        var updateFailure = false
        var updatedName: String? = null

        override suspend fun getStoreDisplayName(): String? {
            if (nameFailure) error("boom")
            return "Loja A"
        }

        override suspend fun updateStoreDisplayName(displayName: String) {
            if (updateFailure) error("boom")
            updatedName = displayName
        }
    }
}
