package com.cabral.lucrovarejo.ui.screens.forgotpassword

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.usecase.RequestPasswordResetUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAuthRepository()
        viewModel = ForgotPasswordViewModel(RequestPasswordResetUseCase(repository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun blankEmailShowsErrorWithoutCallingRepository() = runTest(dispatcher) {
        viewModel.submit()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isEmailSent)
        assertNull(repository.resetEmail)
    }

    @Test
    fun successMarksEmailAsSentAndSendsTrimmedEmail() = runTest(dispatcher) {
        viewModel.onEmailChanged(" owner@example.com ")

        viewModel.submit()
        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("owner@example.com", repository.resetEmail)
        assertFalse(state.isLoading)
        assertTrue(state.isEmailSent)
        assertNull(state.errorMessage)
    }

    @Test
    fun failureShowsErrorAndDoesNotMarkEmailAsSent() = runTest(dispatcher) {
        repository.failure = IllegalStateException("offline")
        viewModel.onEmailChanged("owner@example.com")

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmailSent)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun editingEmailClearsFeedback() = runTest(dispatcher) {
        viewModel.onEmailChanged("owner@example.com")
        viewModel.submit()
        advanceUntilIdle()

        viewModel.onEmailChanged("other@example.com")

        assertFalse(viewModel.uiState.value.isEmailSent)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    private class FakeAuthRepository : AuthRepository {
        override val hasSession: Boolean = false
        override val storeKey: String? = null
        var resetEmail: String? = null
        var failure: Exception? = null

        override suspend fun registerStore(
            email: String,
            password: String,
            displayName: String
        ) = Unit

        override suspend fun signIn(email: String, password: String) = Unit

        override suspend fun getStoreDisplayName(): String? = null

        override suspend fun updateStoreDisplayName(displayName: String) = Unit

        override suspend fun requestPasswordReset(email: String) {
            failure?.let { throw it }
            resetEmail = email
        }

        override fun signOut() = Unit
    }
}
