package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PurchaseRecordMapperTest {
    @Test
    fun mapsEveryFirestoreFieldToPurchaseRecord() {
        val saleDate = Date(1_735_689_600_000)
        val document = documentSnapshot(saleDate)

        assertEquals(
            PurchaseRecord(
                documentId = "purchase-1",
                cod = "A-1",
                name = "Produto",
                cost = 1200,
                description = "Descricao",
                salePrice = 1500,
                quantity = 2,
                saleDate = saleDate,
                paymentMethod = "PIX"
            ),
            document.toPurchaseRecord()
        )
    }

    @Test
    fun rejectsDocumentsWithoutRequiredFields() {
        val document = documentSnapshot(Date()).also {
            `when`(it.getString("name")).thenReturn(null)
        }

        val failure = runCatching { document.toPurchaseRecord() }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertEquals("Purchase purchase-1 has no valid 'name' field", failure?.message)
    }

    @Test
    fun identifiesEveryMissingRequiredField() {
        val missingFields = listOf(
            "cod" to { document: DocumentSnapshot -> `when`(document.getString("cod")).thenReturn(null) },
            "cost" to { document: DocumentSnapshot -> `when`(document.getLong("cost")).thenReturn(null) },
            "description" to {
                document: DocumentSnapshot -> `when`(document.getString("description")).thenReturn(null)
            },
            "sale_price" to {
                document: DocumentSnapshot -> `when`(document.getLong("sale_price")).thenReturn(null)
            },
            "quantity" to {
                document: DocumentSnapshot -> `when`(document.getLong("quantity")).thenReturn(null)
            },
            "sale_date" to {
                document: DocumentSnapshot -> `when`(document.getTimestamp("sale_date")).thenReturn(null)
            },
            "payment_method" to {
                document: DocumentSnapshot -> `when`(document.getString("payment_method")).thenReturn(null)
            }
        )

        missingFields.forEach { (field, removeField) ->
            val document = documentSnapshot(Date())
            removeField(document)

            val failure = runCatching { document.toPurchaseRecord() }.exceptionOrNull()

            assertEquals(
                "Purchase purchase-1 has no valid '$field' field",
                failure?.message
            )
        }
    }

    internal fun documentSnapshot(saleDate: Date): DocumentSnapshot {
        val document = mock(DocumentSnapshot::class.java)
        `when`(document.id).thenReturn("purchase-1")
        `when`(document.getString("cod")).thenReturn("A-1")
        `when`(document.getString("name")).thenReturn("Produto")
        `when`(document.getLong("cost")).thenReturn(1200)
        `when`(document.getString("description")).thenReturn("Descricao")
        `when`(document.getLong("sale_price")).thenReturn(1500)
        `when`(document.getLong("quantity")).thenReturn(2)
        `when`(document.getTimestamp("sale_date")).thenReturn(Timestamp(saleDate))
        `when`(document.getString("payment_method")).thenReturn("PIX")
        return document
    }
}
