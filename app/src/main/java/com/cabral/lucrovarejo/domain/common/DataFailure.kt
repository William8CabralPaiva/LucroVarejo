package com.cabral.lucrovarejo.domain.common

/** Falha de acesso a dados, compartilhada por produtos, vendas e pagamentos. */
class DataFailure(
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
