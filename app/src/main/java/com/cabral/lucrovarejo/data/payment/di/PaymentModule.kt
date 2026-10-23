package com.cabral.lucrovarejo.data.payment.di

import com.cabral.lucrovarejo.data.payment.CardBrandRepositoryImpl
import com.cabral.lucrovarejo.data.payment.PaymentTerminalRepositoryImpl
import com.cabral.lucrovarejo.data.payment.TerminalFeeRepositoryImpl
import com.cabral.lucrovarejo.data.payment.datasource.CardBrandRemoteDataSource
import com.cabral.lucrovarejo.data.payment.datasource.FirebaseCardBrandRemoteDataSource
import com.cabral.lucrovarejo.data.payment.datasource.FirebasePaymentTerminalRemoteDataSource
import com.cabral.lucrovarejo.data.payment.datasource.FirebaseTerminalFeeRemoteDataSource
import com.cabral.lucrovarejo.data.payment.datasource.PaymentTerminalRemoteDataSource
import com.cabral.lucrovarejo.data.payment.datasource.TerminalFeeRemoteDataSource
import com.cabral.lucrovarejo.domain.payment.CardBrandRepository
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
    abstract fun bindCardBrandRemoteDataSource(
        implementation: FirebaseCardBrandRemoteDataSource
    ): CardBrandRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCardBrandRepository(
        implementation: CardBrandRepositoryImpl
    ): CardBrandRepository

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
