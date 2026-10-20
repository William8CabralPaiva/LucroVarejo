package com.cabral.lucrovarejo.domain.purchase

interface SalesRepository {
    // Vendas em ordem decrescente de data, no máximo pageSize por chamada.
    suspend fun getSalesPage(
        filter: SalesFilter,
        cursor: SalesCursor? = null,
        pageSize: Int = SALES_PAGE_SIZE
    ): SalesPage
}
