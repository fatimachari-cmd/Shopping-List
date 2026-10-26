package com.fatimachari.listadecompra.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatimachari.listadecompra.R

@Composable
fun OnboardingScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val verde = Color(0xFF30734F)
    val fondo = Color(0xFFFAFAF8)
    val texto = Color(0xFF263A30)
    val gris = Color(0xFF78847D)

    var paginaActual by remember {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(onClick = onSkip) {
                Text("Saltar", color = verde)
            }
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = when (paginaActual) {
                0 -> "Bienvenido/a a\nMiCesta"
                1 -> "Comparte\ntus listas"
                else -> "Marca\nlo comprado"
            },
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = if (paginaActual == 0) verde else texto,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(250.dp)
                .background(
                    color = Color(0xFFE7F2EC),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = when (paginaActual) {
                        0 -> R.drawable.onboarding_cesta
                        1 -> R.drawable.onboarding_compartir
                        else -> R.drawable.onboarding_comprado
                    }
                ),
                contentDescription = when (paginaActual) {
                    0 -> "Bolsa de compra con alimentos"
                    1 -> "Lista con símbolo de compartir"
                    else -> "Lista de productos comprados"
                },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = when (paginaActual) {
                0 -> "Crea tu lista de compra y organiza tus productos por categorías."
                1 -> "Comparte tus listas con otras personas y organiza la compra en equipo."
                else -> "Marca los productos comprados sin eliminarlos de tu lista."
            },
            fontSize = 16.sp,
            color = gris,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .width(
                            if (paginaActual == index) 22.dp else 7.dp
                        )
                        .height(7.dp)
                        .background(
                            color = if (paginaActual == index) {
                                verde
                            } else {
                                Color(0xFFD5DDD7)
                            },
                            shape = RoundedCornerShape(50)
                        )
                )

                if (index < 2) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (paginaActual < 2) {
                    paginaActual++
                } else {
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = verde
            )
        ) {
            Text(
                text = if (paginaActual == 2) {
                    "Empezar"
                } else {
                    "Siguiente"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

