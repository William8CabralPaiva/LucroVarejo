package com.cabral.lucrovarejo.navigation

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppNavGraphContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun unauthenticatedFlowNavigatesBetweenSplashLoginRegistrationAndHome() {
        var signedOut = false
        setTestGraph(isUserAuthenticated = false, onSignOut = { signedOut = true })

        composeRule.onNodeWithText("Finish splash").performClick()
        composeRule.onNodeWithText("Test login").assertIsDisplayed()

        composeRule.onNodeWithText("Test login").performClick()
        composeRule.onNodeWithText("Complete registration").assertIsDisplayed()

        composeRule.onNodeWithText("Complete registration").performClick()
        composeRule.onNodeWithText("Test home").assertIsDisplayed()

        composeRule.onNodeWithText("Test home").performClick()
        composeRule.onNodeWithText("Test login").assertIsDisplayed()
        composeRule.runOnIdle { assertTrue(signedOut) }
    }

    @Test
    fun authenticatedSplashSkipsLogin() {
        setTestGraph(isUserAuthenticated = true)

        composeRule.onNodeWithText("Finish splash").performClick()
        composeRule.onNodeWithText("Test home").assertIsDisplayed()
    }

    @Test
    fun successfulLoginNavigatesDirectlyToHome() {
        setTestGraph(isUserAuthenticated = false, loginNavigatesHome = true)

        composeRule.onNodeWithText("Finish splash").performClick()
        composeRule.onNodeWithText("Test login").performClick()

        composeRule.onNodeWithText("Test home").assertIsDisplayed()
    }

    @Test
    fun forgotPasswordRouteOpensAndGoesBack() {
        composeRule.setContent {
            AppNavGraphContent(
                navController = rememberNavController(),
                startDestination = NotLoggedRoutes.LOGIN,
                isUserAuthenticated = false,
                onSignOut = {},
                splashScreen = {},
                loginScreen = { _, _, goToForgotPassword ->
                    TestAction("Test login", goToForgotPassword)
                },
                registerScreen = { _, _ -> },
                loggedFlowScreen = {},
                forgotPasswordScreen = { onBack -> TestAction("Test forgot", onBack) }
            )
        }

        composeRule.onNodeWithText("Test login").performClick()
        composeRule.onNodeWithText("Test forgot").assertIsDisplayed()

        composeRule.onNodeWithText("Test forgot").performClick()
        composeRule.onNodeWithText("Test login").assertIsDisplayed()
    }

    @Test
    fun registrationBackReturnsToLogin() {
        composeRule.setContent {
            AppNavGraphContent(
                navController = rememberNavController(),
                startDestination = NotLoggedRoutes.LOGIN,
                isUserAuthenticated = false,
                onSignOut = {},
                splashScreen = {},
                loginScreen = { goToRegister, _, _ -> TestAction("Test login", goToRegister) },
                registerScreen = { goBack, _ -> TestAction("Go back to login", goBack) },
                loggedFlowScreen = {}
            )
        }

        composeRule.onNodeWithText("Test login").performClick()
        composeRule.onNodeWithText("Go back to login").performClick()

        composeRule.onNodeWithText("Test login").assertIsDisplayed()
    }

    private fun setTestGraph(
        isUserAuthenticated: Boolean,
        onSignOut: () -> Unit = {},
        loginNavigatesHome: Boolean = false
    ) {
        composeRule.setContent {
            AppNavGraphContent(
                navController = rememberNavController(),
                startDestination = NotLoggedRoutes.SPLASH,
                isUserAuthenticated = isUserAuthenticated,
                onSignOut = onSignOut,
                splashScreen = { onFinish ->
                    TestAction("Finish splash", onFinish)
                },
                loginScreen = { goToRegister, goToHome, _ ->
                    TestAction(
                        text = "Test login",
                        onClick = if (loginNavigatesHome) goToHome else goToRegister
                    )
                },
                registerScreen = { goBack, onSuccess ->
                    TestAction("Go back to login", goBack)
                    TestAction("Complete registration", onSuccess)
                },
                loggedFlowScreen = { onLogout ->
                    TestAction("Test home", onLogout)
                }
            )
        }
    }

    @Composable
    private fun TestAction(text: String, onClick: () -> Unit) {
        Text(text = text, modifier = Modifier.clickable(onClick = onClick))
    }
}
