package com.cabral.lucrovarejo.ui.screens.register

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.auth.AuthFailure
import com.cabral.lucrovarejo.domain.auth.AuthFailureReason
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.StoreAlreadyRegisteredException
import com.cabral.lucrovarejo.domain.auth.usecase.RegisterStoreUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val auth = FakeAuth()

    private val google = FakeGoogleAuthRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = RegisterViewModel(RegisterStoreUseCase(auth), SignInWithGoogleUseCase(google))

    @Test
    fun googleSignUpUsesStoreNameFromForm() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onDisplayNameChanged(" Loja Google ")
        vm.onGoogleIdToken("token")
        advanceUntilIdle()

        assertEquals(listOf("token" to "Loja Google"), google.signInCalls)
        assertTrue(vm.uiState.value.isRegistered)
    }

    @Test
    fun googleSignUpWithoutStoreNameIsRejected() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onGoogleIdToken("token")
        advanceUntilIdle()

        assertEquals(R.string.register_display_name_required, vm.uiState.value.displayNameError)
        assertTrue(google.signInCalls.isEmpty())
        assertFalse(vm.uiState.value.isRegistered)
    }

    @Test
    fun googleFailureShowsError() = runTest(dispatcher) {
        google.failure = true
        val vm = viewModel()
        vm.onDisplayNameChanged("Loja")
        vm.onGoogleIdToken("token")
        advanceUntilIdle()

        assertEquals(R.string.register_error_generic, vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    private fun RegisterViewModel.fillValid() {
        onDisplayNameChanged(" Loja ")
        onEmailChanged(" a@b.com ")
        onPasswordChanged("123456")
        onConfirmPasswordChanged("123456")
    }

    @Test
    fun fieldChangesValidateAndPasswordRechecksConfirmation() {
        val vm = viewModel()
        vm.onDisplayNameChanged("")
        vm.onEmailChanged("x")
        vm.onPasswordChanged("123")
        vm.onConfirmPasswordChanged("12")

        val state = vm.uiState.value
        assertEquals(R.string.register_display_name_required, state.displayNameError)
        assertEquals(R.string.register_email_invalid, state.emailError)
        assertEquals(R.string.register_password_too_short, state.passwordError)
        assertEquals(R.string.register_password_mismatch, state.confirmPasswordError)

        vm.onPasswordChanged("12")
        assertNull(vm.uiState.value.confirmPasswordError)
    }

    @Test
    fun registerWithInvalidFormShowsAllErrorsAndSkipsAuth() = runTest(dispatcher) {
        val vm = viewModel()
        vm.register()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(R.string.register_display_name_required, state.displayNameError)
        assertEquals(R.string.register_email_required, state.emailError)
        assertEquals(R.string.register_password_required, state.passwordError)
        assertEquals(R.string.register_confirm_password_required, state.confirmPasswordError)
        assertTrue(auth.calls.isEmpty())
    }

    @Test
    fun registerSuccessTrimsAndMarksRegistered() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fillValid()
        vm.register()
        assertTrue(vm.uiState.value.isLoading)
        advanceUntilIdle()

        assertEquals(listOf(Triple("a@b.com", "123456", "Loja")), auth.calls)
        assertTrue(vm.uiState.value.isRegistered)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isFormValid)
    }

    @Test
    fun registerMapsFailuresToMessages() = runTest(dispatcher) {
        val cases = listOf(
            StoreAlreadyRegisteredException() to R.string.register_store_taken,
            AuthFailure(AuthFailureReason.PROVIDER_DISABLED) to R.string.register_error_provider_disabled,
            AuthFailure(AuthFailureReason.INVALID_ACCOUNT_IDENTIFIER) to R.string.register_error_invalid_identifier,
            AuthFailure(AuthFailureReason.PERMISSION_DENIED) to R.string.register_error_permission_denied,
            AuthFailure(AuthFailureReason.NETWORK) to R.string.register_error_network,
            AuthFailure(AuthFailureReason.UNKNOWN) to R.string.register_error_generic,
            RuntimeException() to R.string.register_error_generic
        )
        for ((failure, message) in cases) {
            auth.failure = failure
            val vm = viewModel()
            vm.fillValid()
            vm.register()
            advanceUntilIdle()

            assertEquals(message, vm.uiState.value.errorMessage)
            assertFalse(vm.uiState.value.isLoading)
            assertFalse(vm.uiState.value.isRegistered)
        }
    }

    private class FakeAuth(
        delegate: AuthRepository = mock(AuthRepository::class.java)
    ) : AuthRepository by delegate {
        val calls = mutableListOf<Triple<String, String, String>>()
        var failure: Exception? = null

        override suspend fun registerStore(email: String, password: String, displayName: String) {
            failure?.let { throw it }
            calls += Triple(email, password, displayName)
        }
    }
}
