package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
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

    private class FakePurchaseFirestoreClient : PurchaseFirestoreClient {
        var storeKey: String? = null
        var request: PurchaseRequest? = null

        override suspend fun savePurchase(
            storeKey: String,
            request: PurchaseRequest
        ): String {
            this.storeKey = storeKey
            this.request = request
            return "purchase-1"
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
