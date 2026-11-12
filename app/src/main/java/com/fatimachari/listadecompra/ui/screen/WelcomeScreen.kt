package com.fatimachari.listadecompra.ui.screen

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatimachari.listadecompra.ui.theme.*

@Composable
fun WelcomeScreen(
    onEmpezar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .systemBarsPadding()
            .padding(24.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PrincipalClaro, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBasket,
                    contentDescription = null,
                    tint = Principal
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("MiCesta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Texto)
        }

        Spacer(modifier = Modifier.height(48.dp))


        Text("Haz la compra en equipo", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Texto)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Organiza tus listas y compártelas con quien quieras.", fontSize = 16.sp, color = Gris)

        Spacer(modifier = Modifier.height(36.dp))


        Ventaja(Icons.Filled.Checklist, "Listas por categorías", "Crea, edita y marca productos.")
        Ventaja(Icons.Filled.Group, "Compártelas por correo", "Invita a alguien y la editáis juntos.")
        Ventaja(Icons.Filled.AutoAwesome, "Clasificación automática", "\"Tomate\" va solo a Frutas y verduras.")

        Spacer(modifier = Modifier.weight(1f))


        Button(
            onClick = onEmpezar,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Principal)
        ) {
            Text("Empezar", fontSize = 17.sp)
        }
    }
}

@Composable
fun Ventaja(icono: ImageVector, titulo: String, descripcion: String) {
    Row(
        modifier = Modifier.padding(bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(PrincipalClaro, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = Principal)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Texto)
            Text(descripcion, fontSize = 14.sp, color = Gris)
        }
    }
}