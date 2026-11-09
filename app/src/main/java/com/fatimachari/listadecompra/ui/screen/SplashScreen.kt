package com.fatimachari.listadecompra.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fatimachari.listadecompra.ui.theme.*
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Principal),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .offset(x = (-150).dp, y = (-280).dp)
                .size(170.dp)
                .background(PrincipalOscuro, CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = 115.dp, y = (-170).dp)
                .size(30.dp)
                .background(PrincipalClaro, CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = 150.dp, y = 270.dp)
                .size(150.dp)
                .background(Acento, CircleShape)
        )


        Column(horizontalAlignment = Alignment.CenterHorizontally) {


            Box(
                modifier = Modifier
                    .size(125.dp)
                    .background(Blanco, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBasket,
                    contentDescription = "Logo de MiCesta",
                    tint = Principal,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("MiCesta", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Blanco)
        }
    }
}