package com.cabral.lucrovarejo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cabral.lucrovarejo.domain.auth.usecase.HasSessionUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.SignOutUseCase
import com.cabral.lucrovarejo.navigation.AppNavGraph
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var hasSession: HasSessionUseCase

    @Inject
    lateinit var signOut: SignOutUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }

            LucroVarejoTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        isUserAuthenticated = hasSession(),
                        onSignOut = { signOut() },
                        currentThemeMode = themeMode,
                        onThemeModeChanged = { newMode ->
                            themeMode = newMode
                        }
                    )
                }
            }
        }
    }
}
