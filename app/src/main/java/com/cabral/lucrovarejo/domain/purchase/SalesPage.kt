package com.cabral.lucrovarejo.domain.purchase

import java.util.Date

const val SALES_PAGE_SIZE = 10

// dateStart é inclusivo e dateEnd é exclusivo; ambos nulos significam "todas as datas".
data class SalesFilter(
    val nameQuery: String = "",
    val dateStart: Date? = null,
    val dateEnd: Date? = null
)

// Posição da última venda entregue: a próxima página começa logo depois dela.
data class SalesCursor(
    val saleDate: Date,
    val documentId: String
)

data class SalesPage(
    val items: List<PurchaseModel>,
    val nextCursor: SalesCursor?
) {
    val hasMore: Boolean get() = nextCursor != null
}
