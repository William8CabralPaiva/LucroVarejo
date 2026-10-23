package com.cabral.lucrovarejo.data.payment.di

import com.cabral.lucrovarejo.data.payment.PaymentTerminalRepositoryImpl
import com.cabral.lucrovarejo.data.payment.TerminalFeeRepositoryImpl
import com.cabral.lucrovarejo.data.payment.datasource.FirebasePaymentTerminalRemoteDataSource
import com.cabral.lucrovarejo.data.payment.datasource.FirebaseTerminalFeeRemoteDataSource
import com.cabral.lucrovarejo.data.payment.datasource.PaymentTerminalRemoteDataSource
import com.cabral.lucrovarejo.data.payment.datasource.TerminalFeeRemoteDataSource
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import com.cabral.lucrovarejo.domain.payment.TerminalFeeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentModule {
    @Binds
    @Singleton
    abstract fun bindPaymentTerminalRemoteDataSource(
        implementation: FirebasePaymentTerminalRemoteDataSource
    ): PaymentTerminalRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindPaymentTerminalRepository(
        implementation: PaymentTerminalRepositoryImpl
    ): PaymentTerminalRepository

    @Binds
    @Singleton
    abstract fun bindTerminalFeeRemoteDataSource(
        implementation: FirebaseTerminalFeeRemoteDataSource
    ): TerminalFeeRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindTerminalFeeRepository(
        implementation: TerminalFeeRepositoryImpl
    ): TerminalFeeRepository
}
