package com.cabral.lucrovarejo.data.store.di

import com.cabral.lucrovarejo.data.store.ActiveStoreHolder
import com.cabral.lucrovarejo.data.store.ActiveStoreKeyProvider
import com.cabral.lucrovarejo.data.store.StoreRepositoryImpl
import com.cabral.lucrovarejo.domain.store.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StoreModule {
    @Binds
    @Singleton
    abstract fun bindActiveStoreKeyProvider(implementation: ActiveStoreHolder): ActiveStoreKeyProvider

    @Binds
    @Singleton
    abstract fun bindStoreRepository(implementation: StoreRepositoryImpl): StoreRepository
}
