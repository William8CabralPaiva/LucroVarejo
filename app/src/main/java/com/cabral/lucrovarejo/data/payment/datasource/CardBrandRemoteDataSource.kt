package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.payment.model.CardBrandRecord
import com.cabral.lucrovarejo.data.payment.model.CardBrandRequest
import kotlinx.coroutines.flow.Flow

interface CardBrandRemoteDataSource {
    suspend fun createCardBrand(request: CardBrandRequest): String
    suspend fun renameCardBrand(key: String, name: String)
    suspend fun setCardBrandActive(key: String, active: Boolean)
    fun observeCardBrands(): Flow<List<CardBrandRecord>>
}
