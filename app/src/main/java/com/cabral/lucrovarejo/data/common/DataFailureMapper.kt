package com.cabral.lucrovarejo.data.common

import com.cabral.lucrovarejo.domain.common.DataFailure
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/** Normaliza erros do Firestore para [DataFailure]; compartilhado por todos os repositórios. */
internal fun Throwable.toDataFailure(): DataFailure = when (this) {
    is DataFailure -> this
    is FirebaseFirestoreException -> {
        val reason = when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                DataFailure.Reason.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE -> DataFailure.Reason.NETWORK
            else -> DataFailure.Reason.UNKNOWN
        }
        DataFailure(reason, this)
    }
    else -> DataFailure(DataFailure.Reason.UNKNOWN, this)
}

internal fun <T> Flow<T>.withDataFailure(): Flow<T> =
    catch { failure -> throw failure.toDataFailure() }

internal suspend fun <T> withDataFailure(operation: suspend () -> T): T = try {
    operation()
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: Exception) {
    throw failure.toDataFailure()
}
