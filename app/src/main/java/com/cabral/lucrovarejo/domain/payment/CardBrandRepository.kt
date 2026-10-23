package com.cabral.lucrovarejo.domain.payment

import kotlinx.coroutines.flow.Flow

interface CardBrandRepository {
    suspend fun createCardBrand(brand: CardBrandModel): CardBrandModel
    suspend fun renameCardBrand(key: String, name: String): CardBrandModel
    suspend fun archiveCardBrand(key: String)
    fun observeCardBrands(): Flow<List<CardBrandModel>>
}
