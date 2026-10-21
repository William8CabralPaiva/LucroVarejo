package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.domain.product.ProductModel
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
) {
    val isEditing: Boolean
        get() = key != null

    val canSubmit: Boolean
        get() = form.selectedProduct != null &&
                parseSalePrice(form.salePrice) != null &&
                form.paymentMethod != null &&
                !isLoadingProducts &&
                !isSaving

    val profit: BigDecimal?
        get() {
            val product = form.selectedProduct ?: return null
            val price = parseSalePrice(form.salePrice) ?: return null
            return price.subtract(product.cost).multiply(BigDecimal.valueOf(form.quantity.toLong()))
        }
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
