package com.cabral.lucrovarejo.domain.purchase

import kotlinx.coroutines.flow.Flow

interface SalesRepository {
    // Vendas em ordem decrescente de data, no máximo pageSize por chamada.
    suspend fun getSalesPage(
        filter: SalesFilter,
        cursor: SalesCursor? = null,
        pageSize: Int = SALES_PAGE_SIZE
    ): SalesPage

    fun observeSalesChanges(filter: SalesFilter): Flow<Unit>
}
