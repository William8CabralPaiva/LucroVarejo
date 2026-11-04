package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.usecase.calculateTariffAmountCents
import com.cabral.lucrovarejo.domain.payment.usecase.endOfLocalDay
import com.cabral.lucrovarejo.domain.payment.usecase.toTerminalType
import com.cabral.lucrovarejo.domain.product.ProductModel
import java.util.Date
import java.math.BigDecimal
import java.math.RoundingMode

data class AddPurchaseUiState(
    val products: List<ProductModel> = emptyList(),
    val form: PurchaseFormState = PurchaseFormState(),
    val isLoadingProducts: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: Int? = null,
    val formResetCount: Int = 0,
    val key: String? = null,
    val showDeleteDialog: Boolean = false,
    val terminals: List<PaymentTerminalModel> = emptyList(),
    // Tarifas da maquininha selecionada (ou de todas as que aceitam dinheiro, quando a forma for dinheiro).
    val tariffs: List<PaymentMachineModel> = emptyList(),
    val isLoadingTariffs: Boolean = false,
    val appliedTariff: AppliedTariff = AppliedTariff.NONE,
    // Em edição o snapshot salvo vale só até as tarifas atuais carregarem (ou o usuário alterar a seleção).
    val keepSavedTariff: Boolean = false,
    val tariffNotice: Int? = null,
) {
    val isEditing: Boolean
        get() = key != null

    val canSubmit: Boolean
        get() = form.selectedProduct != null &&
                parseSalePrice(form.salePrice) != null &&
                form.isPaymentSelectionComplete &&
                !isLoadingProducts &&
                !isLoadingTariffs &&
                !isSaving

    val profit: BigDecimal?
        get() {
            val product = form.selectedProduct ?: return null
            val price = parseSalePrice(form.salePrice) ?: return null
            return price.subtract(product.cost).multiply(BigDecimal.valueOf(form.quantity.toLong()))
        }

    val terminalOptions: List<PaymentTerminalModel>
        get() {
            val method = form.paymentMethod?.takeIf { it.needsTerminal() } ?: return emptyList()
            val type = method.toTerminalType()
            return terminals.filter { it.active && type in it.enabledMethods }
        }

    val brandOptions: List<String>
        get() {
            val method = form.paymentMethod?.takeIf { it.needsBrand() } ?: return emptyList()
            val terminal = selectedTerminal() ?: return emptyList()
            val withTariff = effectiveTariffs(method.toTerminalType())
                .mapNotNull { it.brandId }
                .toSet()
            return terminal.brands.filter { it in withTariff }
        }

    val installmentOptions: List<Int>
        get() {
            if (form.paymentMethod != PaymentMethods.CREDIT || form.brand == null) return emptyList()
            return effectiveTariffs(TerminalPaymentType.CREDIT)
                .filter { it.brandId == form.brand }
                .mapNotNull { it.installments }
                .distinct()
                .sorted()
        }

    val showTerminal: Boolean
        get() = form.paymentMethod?.needsTerminal() == true

    val showBrand: Boolean
        get() = form.paymentMethod?.needsBrand() == true

    val showInstallments: Boolean
        get() = form.paymentMethod == PaymentMethods.CREDIT

    val tariffAmount: BigDecimal?
        get() = appliedTariff.takeIf { it.rateBps > 0 }
            ?.let { BigDecimal.valueOf(tariffAmountCents(), 2) }

    fun tariffAmountCents(): Long {
        val price = parseSalePrice(form.salePrice) ?: return 0L
        val totalCents = price.movePointRight(2).longValueExact() * form.quantity
        return calculateTariffAmountCents(totalCents, appliedTariff.rateBps)
    }

    private fun selectedTerminal(): PaymentTerminalModel? =
        terminals.firstOrNull { it.key != null && it.key == form.terminalId }

    private fun effectiveTariffs(type: TerminalPaymentType): List<PaymentMachineModel> {
        val cutoff = Date(form.saleDateMillis).endOfLocalDay()
        return tariffs.filter { it.method == type && !it.effectiveFrom.after(cutoff) }
    }
}

data class AppliedTariff(val tariffId: String?, val rateBps: Int) {
    companion object {
        val NONE = AppliedTariff(tariffId = null, rateBps = 0)
    }
}

internal fun PaymentMethods.needsTerminal(): Boolean = this != PaymentMethods.CASH

internal fun PaymentMethods.needsBrand(): Boolean =
    this == PaymentMethods.DEBIT || this == PaymentMethods.CREDIT

/** Todos os spinners exigidos pela forma de pagamento escolhida estão preenchidos. */
val PurchaseFormState.isPaymentSelectionComplete: Boolean
    get() = when (paymentMethod) {
        null -> false
        PaymentMethods.CASH -> true
        PaymentMethods.PIX -> terminalId != null
        PaymentMethods.DEBIT -> terminalId != null && brand != null
        PaymentMethods.CREDIT -> terminalId != null && brand != null && installments != null
    }

internal fun parseSalePrice(value: String): BigDecimal? = try {
    BigDecimal(value.trim().replace(',', '.'))
        .setScale(2, RoundingMode.UNNECESSARY)
        .takeIf { it > BigDecimal.ZERO }
} catch (_: NumberFormatException) {
    null
} catch (_: ArithmeticException) {
    null
}
