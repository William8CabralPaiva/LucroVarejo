package com.cabral.lucrovarejo.data.payment

import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.google.firebase.firestore.FirebaseFirestoreException

/** Compartilhado pelos repositórios de pagamento para normalizar erros do Firestore. */
internal fun Throwable.toPaymentFailure(): PaymentFailure = when (this) {
    is PaymentFailure -> this
    is FirebaseFirestoreException -> {
        val reason = when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                PaymentFailure.Reason.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE -> PaymentFailure.Reason.NETWORK
            else -> PaymentFailure.Reason.UNKNOWN
        }
        PaymentFailure(reason, this)
    }
    else -> PaymentFailure(PaymentFailure.Reason.UNKNOWN, this)
}
