package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.SalesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSalesChangesUseCase @Inject constructor(
    private val repository: SalesRepository
) {
    operator fun invoke(filter: SalesFilter): Flow<Unit> = repository.observeSalesChanges(filter)
}
