package com.cabral.lucrovarejo.data.auth.di

import com.cabral.lucrovarejo.data.auth.datasource.AuthRemoteDataSource
import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import com.cabral.lucrovarejo.data.auth.di.AuthDataSourceModule
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.usecase.RegisterStoreUseCase
import dagger.hilt.android.testing.BindValue
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

    @BindValue
    @JvmField
    val fakeRemoteDataSource: AuthRemoteDataSource = FakeAuthRemoteDataSource()

    @Inject
    lateinit var registerStore: RegisterStoreUseCase

    @Inject
    lateinit var repository: AuthRepository

    @Before
    fun injectDependencies() {
        hiltRule.inject()
    }

    @Test
    fun hiltWiresUseCaseRepositoryAndTestDataSource() = runTest {
        registerStore(" Mi-Loja.1 ", " contato@exemplo.com ", "senha123")

        val fake = fakeRemoteDataSource as FakeAuthRemoteDataSource
        assertEquals("mi-loja.1@auth.lucrovarejo.invalid", fake.savedTechnicalEmail)
        assertEquals("senha123", fake.savedPassword)
        assertEquals(
            StoreProfile("mi-loja.1", "contato@exemplo.com"),
            fake.savedProfile
        )
        assertEquals(repository.hasSession, fake.hasSession)
    }
}