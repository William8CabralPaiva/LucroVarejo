package com.cabral.lucrovarejo.data.product.di

import com.cabral.lucrovarejo.data.product.ProductRepositoryImpl
import com.cabral.lucrovarejo.data.product.datasource.FirebaseProductRemoteDataSource
import com.cabral.lucrovarejo.data.product.datasource.ProductRemoteDataSource
import com.cabral.lucrovarejo.domain.product.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProductModule {
    @Binds
    @Singleton
    abstract fun bindProductRemoteDataSource(
        implementation: FirebaseProductRemoteDataSource
    ): ProductRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        implementation: ProductRepositoryImpl
    ): ProductRepository
}
