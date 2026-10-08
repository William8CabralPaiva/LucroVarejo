package com.cabral.lucrovarejo.data.auth.di

import com.cabral.lucrovarejo.data.auth.datasource.AuthRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AuthDataSourceModule::class]
)
abstract class TestAuthDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindFakeAuthRemoteDataSource(
        fake: FakeAuthRemoteDataSource
    ): AuthRemoteDataSource
}
