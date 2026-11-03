package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.SALES_PAGE_SIZE
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.SalesPage
import com.cabral.lucrovarejo.domain.purchase.SalesRepository
import javax.inject.Inject

class GetSalesPageUseCase @Inject constructor(
    private val repository: SalesRepository
) {
    suspend operator fun invoke(
        filter: SalesFilter,
        cursor: SalesCursor? = null,
        pageSize: Int = SALES_PAGE_SIZE
    ): SalesPage = repository.getSalesPage(filter, cursor, pageSize)
}
