package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import java.util.Date

interface SalesRemoteDataSource {
    // Vendas por sale_date decrescente, a partir de depois de `after`, filtradas pelo intervalo.
    suspend fun fetchSales(
        dateStart: Date?,
        dateEnd: Date?,
        after: SalesCursor?,
        limit: Int
    ): List<PurchaseRecord>
}
