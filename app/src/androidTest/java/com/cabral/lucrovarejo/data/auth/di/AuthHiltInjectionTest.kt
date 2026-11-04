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
        registerStore(" contato@exemplo.com ", "senha123", " Mi Loja ")

        assertEquals("contato@exemplo.com", fakeRemoteDataSource.savedTechnicalEmail)
        assertEquals("senha123", fakeRemoteDataSource.savedPassword)
        assertEquals(
            StoreProfile("contato@exemplo.com", "Mi Loja"),
            fakeRemoteDataSource.savedProfile
        )
        assertEquals(repository.hasSession, fakeRemoteDataSource.hasSession)
    }

    @Test
    fun signInTrimsEmail() = runTest {
        repository.signIn(" contato@exemplo.com ", "senha123")
        assertEquals("contato@exemplo.com", fakeRemoteDataSource.signedInTechnicalEmail)
    }

    @Test
    fun passwordResetReachesRemoteDataSourceWithTrimmedEmail() = runTest {
        repository.requestPasswordReset(" contato@exemplo.com ")

        assertEquals("contato@exemplo.com", fakeRemoteDataSource.resetEmail)
    }
}
