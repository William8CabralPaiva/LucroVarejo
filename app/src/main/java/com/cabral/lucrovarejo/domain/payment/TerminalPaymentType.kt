package com.cabral.lucrovarejo.domain.payment

/**
 * Tipos de pagamento habilitáveis numa maquininha (independente de [PaymentMethods],
 * usado hoje pelo fluxo de Nova Venda). Débito e crédito são tratados separadamente
 * porque cada um tem regras de tarifa diferentes (crédito varia por parcela).
 */
enum class TerminalPaymentType {
    CASH,
    PIX,
    DEBIT,
    CREDIT
}
