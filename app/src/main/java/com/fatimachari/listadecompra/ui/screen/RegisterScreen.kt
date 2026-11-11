package com.fatimachari.listadecompra.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatimachari.listadecompra.R
import com.fatimachari.listadecompra.ui.theme.*

@Composable
fun RegisterScreen(
    onRegister: (nombre: String, email: String, pass: String) -> Unit,
    onLogin: () -> Unit,
    cargando: Boolean = false,
    errorMsg: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var repetirContrasena by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val redondo = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Blanco, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.login_header_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("MiCesta", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Principal)
        }

        // Formulario
        Column(modifier = Modifier.padding(24.dp)) {

            Text("Crea tu cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Texto)
            Text("Organiza tus compras y compártelas", fontSize = 14.sp, color = Gris)

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Principal) },
                shape = redondo,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Principal) },
                shape = redondo,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Principal) },
                visualTransformation = PasswordVisualTransformation(),
                shape = redondo,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = repetirContrasena,
                onValueChange = { repetirContrasena = it },
                label = { Text("Repite la contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Principal) },
                visualTransformation = PasswordVisualTransformation(),
                shape = redondo,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (error != "") {
                Text(error, color = Rojo, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            } else if (errorMsg != null) {
                Text(errorMsg, color = Rojo, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nombre == "" || correo == "" || contrasena == "" || repetirContrasena == "") {
                        error = "Rellena todos los campos."
                    } else if (contrasena != repetirContrasena) {
                        error = "Las contraseñas no coinciden."
                    } else if (contrasena.length < 6) {
                        error = "La contraseña debe tener al menos 6 caracteres."
                    } else {
                        error = ""
                        onRegister(nombre.trim(), correo.trim(), contrasena)
                    }
                },
                shape = redondo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (cargando) {
                    Text("Cargando...", fontSize = 16.sp)
                } else {
                    Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volver al login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿Ya tienes cuenta?", color = Gris)
                TextButton(onClick = onLogin) {
                    Text("Inicia sesión", color = Principal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}