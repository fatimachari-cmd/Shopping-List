package com.fatimachari.listadecompra

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.fatimachari.listadecompra.auth.AuthRepository
import com.fatimachari.listadecompra.ui.screen.LoginScreen
import com.fatimachari.listadecompra.ui.screen.OnboardingScreen
import com.fatimachari.listadecompra.ui.screen.RegisterScreen
import com.fatimachari.listadecompra.ui.screen.SplashScreen
import com.fatimachari.listadecompra.ui.theme.ListadeCompraTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ListadeCompraTheme {

                val authRepository = remember { AuthRepository() }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                var pantalla by remember {
                    mutableStateOf("splash")
                }

                when (pantalla) {

                    "splash" -> {
                        SplashScreen(
                            onTimeout = {
                                pantalla = "onboarding"
                            }
                        )
                    }

                    "onboarding" -> {
                        OnboardingScreen(
                            onNext = {
                                pantalla = "login"
                            },
                            onSkip = {
                                pantalla = "login"
                            }
                        )
                    }

                    "login" -> {
                        LoginScreen(
                            onLogin = { email, password ->
                                scope.launch {
                                    val resultado = authRepository.loginWithEmail(email, password)
                                    resultado.onSuccess { user ->
                                        Toast.makeText(
                                            context,
                                            "¡Bienvenido/a ${user.email ?: ""}!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.onFailure { error ->
                                        Toast.makeText(
                                            context,
                                            error.localizedMessage ?: "Error al iniciar sesión.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },
                            onRegister = {
                                pantalla = "registro"
                            },
                            onGoogleClick = {
                                scope.launch {
                                    val resultado = authRepository.signInWithGoogle(this@MainActivity)
                                    resultado.onSuccess { user ->
                                        Toast.makeText(
                                            context,
                                            "¡Bienvenido/a ${user.email ?: ""}!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.onFailure { error ->
                                        Toast.makeText(
                                            context,
                                            error.localizedMessage ?: "Error al iniciar sesión con Google.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        )
                    }

                    "registro" -> {
                        RegisterScreen(
                            onRegister = { email, password ->
                                scope.launch {
                                    val resultado = authRepository.registerWithEmail(email, password)
                                    resultado.onSuccess { user ->
                                        Toast.makeText(
                                            context,
                                            "¡Cuenta creada correctamente para ${user.email ?: ""}!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        pantalla = "login"
                                    }.onFailure { error ->
                                        Toast.makeText(
                                            context,
                                            error.localizedMessage ?: "Error al crear cuenta.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },
                            onLogin = {
                                pantalla = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    ListadeCompraTheme {
        LoginScreen(
            onLogin = { _, _ -> },
            onRegister = {},
            onGoogleClick = {}
        )
    }
}
