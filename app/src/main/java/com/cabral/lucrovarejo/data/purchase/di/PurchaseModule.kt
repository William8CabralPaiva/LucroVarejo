package com.cabral.lucrovarejo.data.purchase.di

import com.cabral.lucrovarejo.data.purchase.PurchaseRepositoryImpl
import com.cabral.lucrovarejo.data.purchase.datasource.FirebasePurchaseFirestoreClient
import com.cabral.lucrovarejo.data.purchase.datasource.FirebasePurchaseRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseFirestoreClient
import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseRemoteDataSource
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PurchaseModule {
    @Binds
    @Singleton
    abstract fun bindPurchaseFirestoreClient(
        implementation: FirebasePurchaseFirestoreClient
    ): PurchaseFirestoreClient

    @Binds
    @Singleton
    abstract fun bindPurchaseRemoteDataSource(
        implementation: FirebasePurchaseRemoteDataSource
    ): PurchaseRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindPurchaseRepository(
        implementation: PurchaseRepositoryImpl
    ): PurchaseRepository
}
