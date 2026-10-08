package com.cabral.lucrovarejo.data.auth.di

import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.usecase.RegisterStoreUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AuthHiltInjectionTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var registerStore: RegisterStoreUseCase

    @Inject
    lateinit var repository: AuthRepository

    @Inject
    lateinit var fakeRemoteDataSource: FakeAuthRemoteDataSource

    @Before
    fun injectDependencies() {
        hiltRule.inject()
    }

    @Test
    fun hiltWiresUseCaseRepositoryAndTestDataSource() = runTest {
        registerStore(" Mi-Loja.1 ", " contato@exemplo.com ", "senha123")

        assertEquals(
            "mi-loja.1@auth.lucrovarejo.invalid",
            fakeRemoteDataSource.savedTechnicalEmail
        )
        assertEquals("senha123", fakeRemoteDataSource.savedPassword)
        assertEquals(
            StoreProfile("mi-loja.1", "contato@exemplo.com"),
            fakeRemoteDataSource.savedProfile
        )
        assertEquals(repository.hasSession, fakeRemoteDataSource.hasSession)
    }
}
