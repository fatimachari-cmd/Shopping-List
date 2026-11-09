package com.fatimachari.listadecompra

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.fatimachari.listadecompra.auth.AuthRepository
import com.fatimachari.listadecompra.ui.screen.LoginScreen
import com.fatimachari.listadecompra.ui.screen.MyListScreen
import com.fatimachari.listadecompra.ui.screen.RegisterScreen
import com.fatimachari.listadecompra.ui.screen.SplashScreen
import com.fatimachari.listadecompra.ui.screen.WelcomeScreen
import com.fatimachari.listadecompra.ui.theme.ListadeCompraTheme
import com.fatimachari.listadecompra.ui.theme.listdetail.ListDetailScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ListadeCompraTheme {

                val authRepository = remember {
                    AuthRepository()
                }

                val currentUser by authRepository.currentUser.collectAsState()
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                var pantalla by remember {
                    mutableStateOf("splash")
                }

                var cargando by remember {
                    mutableStateOf(false)
                }

                var errorMsg by remember {
                    mutableStateOf<String?>(null)
                }

                var listaSeleccionada by remember {
                    mutableStateOf("")
                }

                when (pantalla) {

                    "splash" -> {
                        SplashScreen(
                            onTimeout = {
                                pantalla = if (currentUser != null) {
                                    "mis_listas"
                                } else {
                                    "welcome"
                                }
                            }
                        )
                    }

                    "welcome" -> {
                        WelcomeScreen(
                            onEmpezar = {
                                errorMsg = null
                                pantalla = "login"
                            }
                        )
                    }

                    "login" -> {
                        LoginScreen(
                            cargando = cargando,
                            errorMsg = errorMsg,
                            onLogin = { email, password ->

                                cargando = true
                                errorMsg = null

                                scope.launch {
                                    val resultado =
                                        authRepository.loginWithEmail(
                                            email,
                                            password
                                        )

                                    cargando = false

                                    resultado.onSuccess { user ->
                                        Toast.makeText(
                                            context,
                                            "¡Bienvenido/a ${user.email ?: ""}!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        pantalla = "mis_listas"
                                    }.onFailure { error ->
                                        errorMsg =
                                            error.localizedMessage
                                                ?: "Error al iniciar sesión."
                                    }
                                }
                            },
                            onRegister = {
                                errorMsg = null
                                pantalla = "registro"
                            },
                            onGoogleClick = {
                                cargando = true
                                errorMsg = null

                                scope.launch {
                                    val resultado =
                                        authRepository.signInWithGoogle(
                                            this@MainActivity
                                        )

                                    cargando = false

                                    resultado.onSuccess { user ->
                                        Toast.makeText(
                                            context,
                                            "¡Bienvenido/a ${user.email ?: ""}!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        pantalla = "mis_listas"
                                    }.onFailure { error ->
                                        errorMsg =
                                            error.localizedMessage
                                                ?: "Error al iniciar sesión con Google."
                                    }
                                }
                            }
                        )
                    }

                    "registro" -> {
                        RegisterScreen(
                            cargando = cargando,
                            errorMsg = errorMsg,
                            onRegister = { nombre, email, password ->

                                cargando = true
                                errorMsg = null

                                scope.launch {
                                    val resultado =
                                        authRepository.registerWithEmail(
                                            nombre,
                                            email,
                                            password
                                        )

                                    cargando = false

                                    resultado.onSuccess { user ->
                                        Toast.makeText(
                                            context,
                                            "¡Cuenta creada correctamente!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        pantalla = "mis_listas"
                                    }.onFailure { error ->
                                        errorMsg =
                                            error.localizedMessage
                                                ?: "Error al crear cuenta."
                                    }
                                }
                            },
                            onLogin = {
                                errorMsg = null
                                pantalla = "login"
                            }
                        )
                    }

                    "mis_listas" -> {
                        MyListScreen(
                            onListClick = { listId ->
                                listaSeleccionada = listId
                                pantalla = "detalle"
                            },
                            onLogout = {
                                authRepository.signOut()
                                errorMsg = null
                                pantalla = "welcome"
                            }
                        )
                    }

                    "detalle" -> {
                        ListDetailScreen(
                            listId = listaSeleccionada,
                            onBack = {
                                pantalla = "mis_listas"
                            }
                        )
                    }
                }
            }
        }
    }
}