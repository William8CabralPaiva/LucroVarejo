package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.utils.Utils.todayUtcMillis

data class PurchaseFormState(
    val selectedProduct: ProductModel? = null,
    val salePrice: String = "",
    val salePriceError: Int? = null,
    val quantity: Int = 1,
    val paymentMethod: PaymentMethods? = null,
    val saleDateMillis: Long = todayUtcMillis()
)