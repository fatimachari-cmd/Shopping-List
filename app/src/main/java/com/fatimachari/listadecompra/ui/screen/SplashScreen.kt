package com.fatimachari.listadecompra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun SplashScreen( onTimeout: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    val verde = Color(0xFF20583E)
    val verdeClaro = Color(0xFF356F52)
    val amrillo = Color(0xFFE8BE45)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(verde),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .offset(x = (-150).dp, y = (-280).dp)
                .clip(CircleShape)
                .background(verdeClaro)
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .offset(x = 115.dp, y = (-170).dp)
                .clip(CircleShape)
                .background(amrillo)
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 150.dp, y = 270.dp)
                .clip(CircleShape)
                .background(verdeClaro)
        )


        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF65C982).copy(alpha = 0.70f),
                            Color(0xFF4DAD70).copy(alpha = 0.40f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(125.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Logo de CompraVerde",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF356F52)
                )
            }
        }

    }
}

