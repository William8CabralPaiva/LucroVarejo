package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.product.ProductModel

data class PurchaseFormState(
    val selectedProduct: ProductModel? = null,
    val salePrice: String = "",
    val salePriceError: Int? = null,
    val quantity: Int = 1,
    val paymentMethod: PaymentMethods? = null,
    val saleDateMillis: Long = System.currentTimeMillis()
)