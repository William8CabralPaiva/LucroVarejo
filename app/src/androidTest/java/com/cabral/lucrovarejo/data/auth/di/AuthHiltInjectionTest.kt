package com.cabral.lucrovarejo.data.auth.di

import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.usecase.RegisterStoreUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AuthDataSourceModule::class)
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
        registerStore(" Mi-Loja.1 ", " contato@exemplo.com ", "senha123", " Mi Loja ")

        assertEquals("contato@exemplo.com", fakeRemoteDataSource.savedTechnicalEmail)
        assertEquals("senha123", fakeRemoteDataSource.savedPassword)
        assertEquals(
            StoreProfile("mi-loja.1", "contato@exemplo.com", "Mi Loja"),
            fakeRemoteDataSource.savedProfile
        )
        assertEquals(repository.hasSession, fakeRemoteDataSource.hasSession)
    }

    @Test
    fun signInUsesEmailAsIsAndUsernameAsLegacyAddress() = runTest {
        repository.signIn(" contato@exemplo.com ", "senha123")
        assertEquals("contato@exemplo.com", fakeRemoteDataSource.signedInTechnicalEmail)

        repository.signIn(" Mi-Loja.1 ", "senha123")
        assertEquals(
            "mi-loja.1@auth.lucrovarejo.invalid",
            fakeRemoteDataSource.signedInTechnicalEmail
        )
    }

    @Test
    fun passwordResetReachesRemoteDataSourceWithTrimmedEmail() = runTest {
        repository.requestPasswordReset(" contato@exemplo.com ")

        assertEquals("contato@exemplo.com", fakeRemoteDataSource.resetEmail)
    }

    @Test
    fun passwordResetWithStoreNameUsesRegisteredEmail() = runTest {
        fakeRemoteDataSource.emailsByStore["mi-loja.1"] = "contato@exemplo.com"

        repository.requestPasswordReset(" Mi-Loja.1 ")

        assertEquals("contato@exemplo.com", fakeRemoteDataSource.resetEmail)
    }
}
