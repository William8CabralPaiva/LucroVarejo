package com.cabral.lucrovarejo.domain.purchase

class PurchaseFailure(
    val reason: Reason,
    cause: Throwable? = null
) : Exception(cause) {
    enum class Reason {
        NETWORK,
        PERMISSION_DENIED,
        UNAUTHENTICATED,
        UNKNOWN
    }
}
