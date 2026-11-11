package com.cabral.lucrovarejo.ui.screens.login

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.usecase.CancelGoogleRegistrationUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.CompleteGoogleRegistrationUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.SignInUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.SignInWithGoogleUseCase
import com.cabral.lucrovarejo.ui.screens.FakeGoogleAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val auth = FakeAuth()

    private val google = FakeGoogleAuthRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = LoginViewModel(
        SignInUseCase(auth),
        SignInWithGoogleUseCase(google),
        CompleteGoogleRegistrationUseCase(google),
        CancelGoogleRegistrationUseCase(google)
    )

    @Test
    fun googleNewAccountAsksStoreNameAndCreatesStore() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onGoogleIdToken("token")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.needsStoreName)
        assertFalse(vm.uiState.value.isLoggedIn)

        vm.onStoreNameChanged("  ")
        vm.confirmStoreName()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.storeNameError)
        assertTrue(google.createdStores.isEmpty())

        vm.onStoreNameChanged("Loja Google")
        vm.confirmStoreName()
        advanceUntilIdle()

        assertEquals(listOf("Loja Google"), google.createdStores)
        assertTrue(vm.uiState.value.isLoggedIn)
        assertFalse(vm.uiState.value.needsStoreName)
    }

    @Test
    fun googleExistingAccountLogsInWithoutStoreName() = runTest(dispatcher) {
        google.storeExists = true
        val vm = viewModel()
        vm.onGoogleIdToken("token")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isLoggedIn)
        assertFalse(vm.uiState.value.needsStoreName)
    }

    @Test
    fun dismissingStoreNameSignsOut() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onGoogleIdToken("token")
        advanceUntilIdle()
        vm.dismissStoreName()

        assertEquals(1, google.cancelCount)
        assertFalse(vm.uiState.value.needsStoreName)
    }

    @Test
    fun googleFailureShowsError() = runTest(dispatcher) {
        google.failure = true
        val vm = viewModel()
        vm.onGoogleIdToken("token")
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoggedIn)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun blankFieldsShowErrorWithoutCallingAuth() = runTest(dispatcher) {
        val vm = viewModel()
        vm.signIn()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(auth.calls.isEmpty())
    }

    @Test
    fun signInTrimsEmailAndMarksLoggedIn() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onEmailChanged("  loja  ")
        vm.onPasswordChanged("123456")
        vm.signIn()
        assertTrue(vm.uiState.value.isLoading)
        advanceUntilIdle()

        assertEquals(listOf("loja" to "123456"), auth.calls)
        assertTrue(vm.uiState.value.isLoggedIn)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun failureShowsErrorAndEditingClearsIt() = runTest(dispatcher) {
        auth.failure = true
        val vm = viewModel()
        vm.onEmailChanged("loja")
        vm.onPasswordChanged("x")
        vm.signIn()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoggedIn)

        vm.onPasswordChanged("y")
        assertNull(vm.uiState.value.errorMessage)
    }

    private class FakeAuth(
        delegate: AuthRepository = mock(AuthRepository::class.java)
    ) : AuthRepository by delegate {
        val calls = mutableListOf<Pair<String, String>>()
        var failure = false

        override suspend fun signIn(email: String, password: String) {
            if (failure) error("boom")
            calls += email to password
        }
    }
}
