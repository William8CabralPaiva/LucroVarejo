package com.cabral.lucrovarejo.domain.payment

import java.io.Serializable

data class CardBrandModel(
    val name: String,
    val active: Boolean = true,
    val key: String? = null
) : Serializable
