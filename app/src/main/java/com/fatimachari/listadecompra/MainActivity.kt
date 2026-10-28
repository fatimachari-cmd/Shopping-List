package com.fatimachari.listadecompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fatimachari.listadecompra.navigation.AppNavigation
import com.fatimachari.listadecompra.ui.theme.ListadeCompraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListadeCompraTheme {
                AppNavigation()
            }
        }
    }
}
