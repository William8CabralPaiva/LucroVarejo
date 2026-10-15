package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebasePurchaseRemoteDataSourceTest {
    @Test
    fun writesPurchaseUnderAuthenticatedStore() = runTest {
        val client = FakePurchaseFirestoreClient()
        val dataSource = FirebasePurchaseRemoteDataSource(
            FakeAuthRepository("store-uid"),
            client
        )
        val request = PurchaseRequest(
            cod = "A-1",
            name = "Produto",
            cost = 1200,
            description = "Descricao",
            salePrice = 1500,
            quantity = 2,
            saleDate = Date(1_735_689_600_000),
            paymentMethod = PaymentMethods.PIX
        )

        val id = dataSource.createPurchase(request)

        assertEquals("store-uid", client.storeKey)
        assertEquals(request, client.request)
        assertEquals("purchase-1", id)

        val fields = requireNotNull(client.request).toFirestoreFields()
        assertEquals(
            setOf(
                "cod",
                "name",
                "cost",
                "description",
                "sale_price",
                "quantity",
                "sale_date",
                "payment_method"
            ),
            fields.keys
        )
        assertTrue(fields["sale_price"] is Int)
        assertTrue(fields["quantity"] is Int)
        assertEquals(request.saleDate, fields["sale_date"])
        assertEquals("PIX", fields["payment_method"])
    }

    @Test
    fun requiresAuthenticatedStoreBeforeSaving() = runTest {
        val client = FakePurchaseFirestoreClient()
        val dataSource = FirebasePurchaseRemoteDataSource(FakeAuthRepository(null), client)
        val request = PurchaseRequest(
            cod = "A-1",
            name = "Produto",
            cost = 1200,
            description = "Descricao",
            salePrice = 1500,
            quantity = 2,
            saleDate = Date(1_735_689_600_000),
            paymentMethod = PaymentMethods.CARD
        )

        val failure = runCatching { dataSource.createPurchase(request) }.exceptionOrNull()

        assertTrue(failure is PurchaseFailure)
        assertEquals(PurchaseFailure.Reason.UNAUTHENTICATED, (failure as PurchaseFailure).reason)
        assertEquals(null, client.storeKey)
    }

    @Test
    fun fetchesPurchasesForAuthenticatedStoreAndTimestampRange() = runTest {
        val client = FakePurchaseFirestoreClient()
        val dataSource = FirebasePurchaseRemoteDataSource(FakeAuthRepository("store-uid"), client)
        val start = Date(1_760_000_000_000)
        val end = Date(1_762_678_400_000)
        val records = listOf(PurchaseSummaryRecord(cost = 1200, salePrice = 1500, quantity = 3))
        client.purchases = records

        val result = dataSource.getPurchasesBetween(start, end)

        assertEquals("store-uid", client.storeKey)
        assertEquals(start to end, client.requestedRange)
        assertEquals(records, result)
    }

    @Test
    fun fetchesLatestPurchasesForAuthenticatedStoreWithLimitOfFifteen() = runTest {
        val client = FakePurchaseFirestoreClient()
        val dataSource = FirebasePurchaseRemoteDataSource(FakeAuthRepository("store-uid"), client)
        val records = listOf(
            PurchaseRecord(
                documentId = "latest",
                cod = "A-1",
                name = "Bolsa",
                cost = 1200,
                description = "",
                salePrice = 1500,
                quantity = 1,
                saleDate = Date(1_760_000_000_000),
                paymentMethod = "PIX"
            )
        )
        client.latestPurchases = records

        val result = dataSource.getLatestPurchases()

        assertEquals("store-uid", client.storeKey)
        assertEquals(15L, client.latestLimit)
        assertEquals(records, result)
    }

    private class FakePurchaseFirestoreClient : PurchaseFirestoreClient {
        var storeKey: String? = null
        var request: PurchaseRequest? = null
        var purchases: List<PurchaseSummaryRecord> = emptyList()
        var requestedRange: Pair<Date, Date>? = null
        var latestLimit: Long? = null
        var latestPurchases: List<PurchaseRecord> = emptyList()

        override suspend fun savePurchase(
            storeKey: String,
            request: PurchaseRequest
        ): String {
            this.storeKey = storeKey
            this.request = request
            return "purchase-1"
        }

        override suspend fun getPurchasesBetween(
            storeKey: String,
            start: Date,
            end: Date
        ): List<PurchaseSummaryRecord> {
            this.storeKey = storeKey
            requestedRange = start to end
            return purchases
        }

        override suspend fun getLatestPurchases(
            storeKey: String,
            limit: Long
        ): List<PurchaseRecord> {
            this.storeKey = storeKey
            latestLimit = limit
            return latestPurchases
        }
    }

    private class FakeAuthRepository(
        override val storeKey: String?
    ) : AuthRepository {
        override val hasSession: Boolean
            get() = storeKey != null

        override suspend fun registerStore(storeName: String, email: String, password: String) = Unit

        override suspend fun signIn(storeName: String, password: String) = Unit

        override fun signOut() = Unit
    }
}
