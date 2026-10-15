package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.google.firebase.firestore.DocumentSnapshot
import androidx.paging.PagingSource
import java.util.Date

interface PurchaseRemoteDataSource {
    suspend fun createPurchase(request: PurchaseRequest): String
    suspend fun getPurchasesBetween(start: Date, end: Date): List<PurchaseSummaryRecord>
    fun getLatestPurchasesPagingSource(): PagingSource<PurchaseCursor, PurchaseRecord>
}

interface PurchaseFirestoreClient {
    suspend fun savePurchase(storeKey: String, request: PurchaseRequest): String
    suspend fun getPurchasesBetween(
        storeKey: String,
        start: Date,
        end: Date
    ): List<PurchaseSummaryRecord>
    suspend fun getLatestPurchasesPage(
        storeKey: String,
        startAfter: DocumentSnapshot?,
        limit: Long
    ): PurchaseFirestorePage
}

data class PurchaseCursor(val document: DocumentSnapshot?)

data class PurchaseFirestorePage(
    val purchases: List<PurchaseRecord>,
    val lastDocument: DocumentSnapshot?
)
