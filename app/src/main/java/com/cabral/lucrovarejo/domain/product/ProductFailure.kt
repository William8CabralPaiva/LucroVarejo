package com.cabral.lucrovarejo.domain.product

class ProductFailure(
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
