package com.cabral.lucrovarejo.domain.store

data class StoreSummary(
    val id: String,
    // Vazio em contas antigas, sem nome cadastrado.
    val displayName: String
)

data class StoreSelection(
    val stores: List<StoreSummary>,
    val activeStoreId: String?
)

interface StoreRepository {
    suspend fun getStoreSelection(): StoreSelection

    suspend fun createStore(displayName: String): StoreSummary

    fun selectStore(storeId: String)
}
