package com.cabral.lucrovarejo.data.purchase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseCursor
import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchasePagination
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

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

    override suspend fun getMonthlySummary(start: Date, end: Date): MonthlyPurchaseSummary =
        withPurchaseFailure {
            require(start.before(end)) { "Summary start timestamp must be before end timestamp" }
            val purchases = remoteDataSource.getPurchasesBetween(start, end)
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

    override fun getLatestPurchases(): Flow<PagingData<PurchaseModel>> =
        Pager(
            config = PagingConfig(
                pageSize = PurchasePagination.PAGE_SIZE,
                initialLoadSize = PurchasePagination.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PurchaseModelPagingSource(remoteDataSource.getLatestPurchasesPagingSource())
            }
        ).flow

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

private class PurchaseModelPagingSource(
    private val source: PagingSource<PurchaseCursor, PurchaseRecord>
) : PagingSource<PurchaseCursor, PurchaseModel>() {
    init {
        source.registerInvalidatedCallback(::invalidate)
    }

    override suspend fun load(
        params: LoadParams<PurchaseCursor>
    ): LoadResult<PurchaseCursor, PurchaseModel> = try {
        when (val result = source.load(params)) {
            is LoadResult.Page -> LoadResult.Page(
                data = result.data.map(PurchaseRecord::toModel),
                prevKey = result.prevKey,
                nextKey = result.nextKey,
                itemsBefore = result.itemsBefore,
                itemsAfter = result.itemsAfter
            )
            is LoadResult.Error -> LoadResult.Error(result.throwable.toPurchaseFailure())
            is LoadResult.Invalid -> LoadResult.Invalid()
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        LoadResult.Error(failure.toPurchaseFailure())
    }

    override fun getRefreshKey(state: PagingState<PurchaseCursor, PurchaseModel>): PurchaseCursor? =
        null
}

private fun Throwable.toPurchaseFailure(): PurchaseFailure = when (this) {
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
