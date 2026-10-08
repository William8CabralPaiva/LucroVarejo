package com.cabral.lucrovarejo.data.auth.di

import com.cabral.lucrovarejo.data.auth.AuthRepositoryImpl
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: AuthRepositoryImpl): AuthRepository
}
