package com.cabral.lucrovarejo.data.purchase

import com.cabral.lucrovarejo.data.purchase.datasource.SalesRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.SalesPage
import com.cabral.lucrovarejo.domain.purchase.SalesRepository
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class SalesRepositoryImpl @Inject constructor(
    private val remoteDataSource: SalesRemoteDataSource
) : SalesRepository {
    // O Firestore não faz busca por trecho do nome, então a página é montada varrendo as vendas
    // (já ordenadas por data) em lotes e mantendo só as que contêm o termo buscado.
    override suspend fun getSalesPage(
        filter: SalesFilter,
        cursor: SalesCursor?,
        pageSize: Int
    ): SalesPage {
        require(pageSize > 0) { "Sales page size must be positive" }
        val nameQuery = filter.nameQuery.normalizeForSearch()
        val fetchSize = if (nameQuery.isEmpty()) pageSize else pageSize * SEARCH_BATCH_FACTOR
        val items = mutableListOf<PurchaseRecord>()
        var after = cursor

        try {
            while (true) {
                val batch = remoteDataSource.fetchSales(
                    dateStart = filter.dateStart,
                    dateEnd = filter.dateEnd,
                    after = after,
                    limit = fetchSize
                )
                for (record in batch) {
                    if (nameQuery.isNotEmpty() &&
                        !record.name.normalizeForSearch().contains(nameQuery)
                    ) {
                        continue
                    }
                    items += record
                    if (items.size == pageSize) {
                        return SalesPage(
                            items = items.map(PurchaseRecord::toModel),
                            nextCursor = SalesCursor(record.saleDate, record.documentId)
                        )
                    }
                }
                val last = batch.lastOrNull()
                if (batch.size < fetchSize || last == null) {
                    return SalesPage(items.map(PurchaseRecord::toModel), nextCursor = null)
                }
                after = SalesCursor(last.saleDate, last.documentId)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            throw failure.toPurchaseFailure()
        }
    }

    private fun String.normalizeForSearch(): String =
        Normalizer.normalize(trim(), Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .lowercase()

    private companion object {
        const val SEARCH_BATCH_FACTOR = 3
        val DIACRITICS = Regex("\\p{Mn}+")
    }
}
