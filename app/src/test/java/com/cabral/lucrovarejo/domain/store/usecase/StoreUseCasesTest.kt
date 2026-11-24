package com.cabral.lucrovarejo.domain.store.usecase

import com.cabral.lucrovarejo.domain.store.StoreRepository
import com.cabral.lucrovarejo.domain.store.StoreSelection
import com.cabral.lucrovarejo.domain.store.StoreSummary
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StoreUseCasesTest {
    private val selection = StoreSelection(listOf(StoreSummary("1", "Loja")), "1")
    private val created = mutableListOf<String>()
    private val switched = mutableListOf<String>()
    private val repository = object : StoreRepository {
        override suspend fun getStoreSelection() = selection
        override suspend fun createStore(displayName: String): StoreSummary {
            created += displayName
            return StoreSummary("new", displayName)
        }

        override fun selectStore(storeId: String) {
            switched += storeId
        }
    }

    @Test
    fun getSelectionDelegates() = runTest {
        assertEquals(selection, GetStoreSelectionUseCase(repository)())
    }

    @Test
    fun createDelegates() = runTest {
        assertEquals(StoreSummary("new", "X"), CreateStoreUseCase(repository)("X"))
        assertEquals(listOf("X"), created)
    }

    @Test
    fun switchDelegates() {
        SwitchStoreUseCase(repository)("2")
        assertEquals(listOf("2"), switched)
    }
}
