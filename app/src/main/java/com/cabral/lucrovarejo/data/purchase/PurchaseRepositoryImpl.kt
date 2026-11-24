package com.cabral.lucrovarejo.data.purchase

import com.cabral.lucrovarejo.data.common.withDataFailure
import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PurchaseRepositoryImpl @Inject constructor(
    private val remoteDataSource: PurchaseRemoteDataSource
) : PurchaseRepository {
    override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
        val request = PurchaseRequest.from(purchase)
        val documentId = withDataFailure {
            remoteDataSource.createPurchase(request)
        }
        return purchase.copy(key = documentId)
    }

    override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel {
        val key = requireNotNull(purchase.key) { "Purchase key is required to update" }
        withDataFailure {
            remoteDataSource.updatePurchase(key, PurchaseRequest.from(purchase))
        }
        return purchase.copy(key = key)
    }

    override suspend fun deletePurchase(key: String) {
        withDataFailure { remoteDataSource.deletePurchase(key) }
    }

    override fun observeMonthlySummary(start: Date, end: Date): Flow<MonthlyPurchaseSummary> {
        require(start.before(end)) { "Summary start timestamp must be before end timestamp" }
        return remoteDataSource.observePurchasesBetween(start, end)
            .map { purchases ->
                purchases.fold(MonthlyPurchaseSummary()) { summary, purchase ->
                    val quantity = purchase.quantity
                    val sales = Math.multiplyExact(purchase.salePrice, quantity)
                    // A tarifa da maquininha entra no custo e reduz o lucro.
                    val cost = Math.addExact(
                        Math.multiplyExact(purchase.cost, quantity),
                        purchase.tariffAmountCents
                    )
                    summary.copy(
                        totalSales = Math.addExact(summary.totalSales, sales),
                        totalCost = Math.addExact(summary.totalCost, cost),
                        totalTariff = Math.addExact(summary.totalTariff, purchase.tariffAmountCents),
                        totalProfit = Math.addExact(
                            summary.totalProfit,
                            Math.subtractExact(sales, cost)
                        ),
                        productsSold = Math.addExact(summary.productsSold, quantity)
                    )
                }
            }
            .withDataFailure()
    }

    override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> {
        require(limit > 0) { "Latest purchases limit must be positive" }
        return remoteDataSource.observeLatestPurchases(limit)
            .map { records -> records.map(PurchaseRecord::toModel) }
            .withDataFailure()
    }
}
