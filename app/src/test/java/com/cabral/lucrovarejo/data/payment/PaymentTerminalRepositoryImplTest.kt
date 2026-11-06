package com.cabral.lucrovarejo.data.payment

import com.cabral.lucrovarejo.data.payment.datasource.PaymentTerminalRemoteDataSource
import com.cabral.lucrovarejo.data.payment.model.PaymentTerminalRecord
import com.cabral.lucrovarejo.data.payment.model.PaymentTerminalRequest
import com.cabral.lucrovarejo.data.common.toDataFailure
import com.cabral.lucrovarejo.domain.common.DataFailure
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PaymentTerminalRepositoryImplTest {
    private val source = FakeSource()
    private val repository = PaymentTerminalRepositoryImpl(source)

    @Test
    fun createReturnsModelWithGeneratedKey() = runTest {
        val created = repository.createTerminal(PaymentTerminalModel(name = "Stone"))

        assertEquals("doc-1", created.key)
        assertEquals("Stone", created.name)
    }

    @Test
    fun updateRequiresKey() = runTest {
        try {
            repository.updateTerminal(PaymentTerminalModel(name = "Stone"))
            fail()
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun updateAndArchiveDelegate() = runTest {
        val updated = repository.updateTerminal(PaymentTerminalModel(name = "Nova", key = "k"))
        repository.archiveTerminal("k")

        assertEquals("k", updated.key)
        assertEquals("k" to "Nova", source.updated)
        assertEquals("k" to false, source.activeChanges.single())
    }

    @Test
    fun failuresAreMappedToDataFailure() = runTest {
        source.failure = FirebaseFirestoreException("x", FirebaseFirestoreException.Code.UNAVAILABLE)
        try {
            repository.archiveTerminal("k")
            fail()
        } catch (failure: DataFailure) {
            assertEquals(DataFailure.Reason.NETWORK, failure.reason)
        }

        val existing = DataFailure(DataFailure.Reason.UNAUTHENTICATED)
        source.failure = existing
        try {
            repository.archiveTerminal("k")
            fail()
        } catch (failure: DataFailure) {
            assertSame(existing, failure)
        }
    }

    @Test
    fun observeMapsRecords() = runTest {
        source.terminals.value = listOf(
            PaymentTerminalRecord("d", PaymentTerminalRequest("Cielo", true, emptyList(), listOf("Visa")))
        )

        val list = repository.observeTerminals().first()
        assertEquals("d", list.single().key)
        assertEquals(listOf("Visa"), list.single().brands)
        assertEquals("d", repository.observeTerminal("d").first()?.key)

        source.terminals.value = emptyList()
        assertNull(repository.observeTerminal("d").first())
    }

    @Test
    fun observeErrorsAreMapped() = runTest {
        source.observeFailure = true
        try {
            repository.observeTerminals().first()
            fail()
        } catch (failure: DataFailure) {
            assertTrue(failure.reason == DataFailure.Reason.UNKNOWN)
        }
        try {
            repository.observeTerminal("d").first()
            fail()
        } catch (failure: DataFailure) {
            assertTrue(failure.reason == DataFailure.Reason.UNKNOWN)
        }
    }

    @Test
    fun permissionDeniedMapsReason() {
        val failure = FirebaseFirestoreException("x", FirebaseFirestoreException.Code.PERMISSION_DENIED)
            .toDataFailure()
        assertEquals(DataFailure.Reason.PERMISSION_DENIED, failure.reason)
        assertEquals(
            DataFailure.Reason.UNKNOWN,
            FirebaseFirestoreException("x", FirebaseFirestoreException.Code.INTERNAL)
                .toDataFailure().reason
        )
    }

    private class FakeSource : PaymentTerminalRemoteDataSource {
        var failure: Exception? = null
        var observeFailure = false
        var updated: Pair<String, String>? = null
        val activeChanges = mutableListOf<Pair<String, Boolean>>()
        val terminals = MutableStateFlow<List<PaymentTerminalRecord>>(emptyList())
        val single = MutableStateFlow<PaymentTerminalRecord?>(null)

        override suspend fun createTerminal(request: PaymentTerminalRequest): String {
            failure?.let { throw it }
            return "doc-1"
        }

        override suspend fun updateTerminal(key: String, request: PaymentTerminalRequest) {
            updated = key to request.name
        }

        override suspend fun setTerminalActive(key: String, active: Boolean) {
            failure?.let { throw it }
            activeChanges += key to active
        }

        override fun observeTerminals(): Flow<List<PaymentTerminalRecord>> =
            if (observeFailure) flow { error("boom") } else terminals

        override fun observeTerminal(key: String): Flow<PaymentTerminalRecord?> =
            if (observeFailure) flow { error("boom") }
            else MutableStateFlow(terminals.value.firstOrNull { it.documentId == key } ?: single.value)
    }
}
