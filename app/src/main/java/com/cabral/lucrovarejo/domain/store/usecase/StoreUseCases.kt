package com.cabral.lucrovarejo.domain.store.usecase

import com.cabral.lucrovarejo.domain.store.StoreRepository
import com.cabral.lucrovarejo.domain.store.StoreSelection
import com.cabral.lucrovarejo.domain.store.StoreSummary
import javax.inject.Inject

class GetStoreSelectionUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): StoreSelection = repository.getStoreSelection()
}

class CreateStoreUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(displayName: String): StoreSummary =
        repository.createStore(displayName)
}

class SwitchStoreUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    operator fun invoke(storeId: String) = repository.selectStore(storeId)
}
