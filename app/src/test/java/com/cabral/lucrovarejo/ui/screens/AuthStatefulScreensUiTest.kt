package com.cabral.lucrovarejo.ui.screens

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.usecase.RegisterStoreUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.SignInUseCase
import com.cabral.lucrovarejo.ui.screens.login.LOGIN_EMAIL_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.login.LOGIN_PASSWORD_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.login.LoginScreen
import com.cabral.lucrovarejo.ui.screens.login.LoginViewModel
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_BACK_BUTTON_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_CONFIRM_PASSWORD_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_DISPLAY_NAME_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_EMAIL_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_PASSWORD_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.register.RegisterScreen
import com.cabral.lucrovarejo.ui.screens.register.RegisterViewModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1400dp")
class AuthStatefulScreensUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private class FakeAuth(
        delegate: AuthRepository = mock(AuthRepository::class.java)
    ) : AuthRepository by delegate {
        val signIns = mutableListOf<Pair<String, String>>()
        val registrations = mutableListOf<Triple<String, String, String>>()

        override suspend fun signIn(email: String, password: String) {
            signIns += email to password
        }

        override suspend fun registerStore(email: String, password: String, displayName: String) {
            registrations += Triple(email, password, displayName)
        }
    }

    @Test
    fun loginScreenSignsInAndNavigatesToLoggedFlow() {
        val auth = FakeAuth()
        var loggedIn = 0
        var registers = 0
        var forgot = 0
        composeRule.setContent {
            LucroVarejoTheme {
                LoginScreen(
                    goToRegisterScreen = { registers++ },
                    goToLoggedFlow = { loggedIn++ },
                    goToForgotPassword = { forgot++ },
                    viewModel = LoginViewModel(SignInUseCase(auth))
                )
            }
        }

        composeRule.onNodeWithTag(LOGIN_EMAIL_FIELD_TAG).performTextInput("loja")
        composeRule.onNodeWithTag(LOGIN_PASSWORD_FIELD_TAG).performTextInput("123456")
        composeRule.onNodeWithText("Entrar").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf("loja" to "123456"), auth.signIns)
        assertEquals(1, loggedIn)

        composeRule.onNodeWithText("Esqueci a senha").performClick()
        assertEquals(1, forgot)
    }

    @Test
    fun registerScreenSubmitsFormAndNotifiesSuccess() {
        val auth = FakeAuth()
        var success = 0
        var back = 0
        composeRule.setContent {
            LucroVarejoTheme {
                RegisterScreen(
                    onBackPress = { back++ },
                    onRegisterSuccess = { success++ },
                    viewModel = RegisterViewModel(RegisterStoreUseCase(auth))
                )
            }
        }

        composeRule.onNodeWithTag(REGISTER_DISPLAY_NAME_FIELD_TAG).performTextInput("Loja")
        composeRule.onNodeWithTag(REGISTER_EMAIL_FIELD_TAG).performTextInput("a@b.com")
        composeRule.onNodeWithTag(REGISTER_PASSWORD_FIELD_TAG).performTextInput("123456")
        composeRule.onNodeWithTag(REGISTER_CONFIRM_PASSWORD_FIELD_TAG).performTextInput("123456")
        composeRule.onNodeWithTag(com.cabral.lucrovarejo.ui.components.LOADING_BUTTON_TAG).performClick()
        composeRule.waitForIdle()

        assertEquals(1, auth.registrations.size)
        assertEquals(1, success)

        composeRule.onNodeWithTag(REGISTER_BACK_BUTTON_TAG).performClick()
        assertEquals(1, back)
    }
}
