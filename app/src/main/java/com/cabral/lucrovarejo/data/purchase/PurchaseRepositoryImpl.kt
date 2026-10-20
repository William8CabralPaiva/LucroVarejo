package com.cabral.lucrovarejo.data.purchase

import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class PurchaseRepositoryImpl @Inject constructor(
    private val remoteDataSource: PurchaseRemoteDataSource
) : PurchaseRepository {
    override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
        val request = PurchaseRequest.from(purchase)
        val documentId = withPurchaseFailure {
            remoteDataSource.createPurchase(request)
        }
        return purchase.copy(id = documentId)
    }

    override fun observeMonthlySummary(start: Date, end: Date): Flow<MonthlyPurchaseSummary> {
        require(start.before(end)) { "Summary start timestamp must be before end timestamp" }
        return remoteDataSource.observePurchasesBetween(start, end)
            .map { purchases ->
                purchases.fold(MonthlyPurchaseSummary()) { summary, purchase ->
                    val quantity = purchase.quantity
                    val sales = Math.multiplyExact(purchase.salePrice, quantity)
                    val cost = Math.multiplyExact(purchase.cost, quantity)
                    summary.copy(
                        totalSales = Math.addExact(summary.totalSales, sales),
                        totalCost = Math.addExact(summary.totalCost, cost),
                        totalProfit = Math.addExact(
                            summary.totalProfit,
                            Math.subtractExact(sales, cost)
                        ),
                        productsSold = Math.addExact(summary.productsSold, quantity)
                    )
                }
            }
            .withPurchaseFailure()
    }

    override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> {
        require(limit > 0) { "Latest purchases limit must be positive" }
        return remoteDataSource.observeLatestPurchases(limit)
            .map { records -> records.map(PurchaseRecord::toModel) }
            .withPurchaseFailure()
    }

    private fun <T> Flow<T>.withPurchaseFailure(): Flow<T> =
        catch { failure -> throw failure.toPurchaseFailure() }
    private suspend fun <T> withPurchaseFailure(
        operation: suspend () -> T
    ): T = try {
        operation()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: PurchaseFailure) {
        throw failure
    } catch (failure: FirebaseFirestoreException) {
        throw failure.toPurchaseFailure()
    } catch (failure: Exception) {
        throw PurchaseFailure(PurchaseFailure.Reason.UNKNOWN, failure)
    }
}

internal fun Throwable.toPurchaseFailure(): PurchaseFailure = when (this) {
    is PurchaseFailure -> this
    is FirebaseFirestoreException -> {
        val reason = when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                PurchaseFailure.Reason.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                PurchaseFailure.Reason.NETWORK
            else -> PurchaseFailure.Reason.UNKNOWN
        }
        PurchaseFailure(reason, this)
    }
    else -> PurchaseFailure(PurchaseFailure.Reason.UNKNOWN, this)
}
