package com.cabral.lucrovarejo.domain.payment

class PaymentFailure(
    val reason: Reason,
    cause: Throwable? = null
) : Exception(cause) {
    enum class Reason {
        NETWORK,
        PERMISSION_DENIED,
        UNAUTHENTICATED,
        DUPLICATE_EFFECTIVE_DATE,
        UNKNOWN
    }
}
