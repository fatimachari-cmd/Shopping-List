package com.fatimachari.listadecompra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Colores de Material: así los menús, ventanas, pestañas y campos usan nuestro verde
private val ColoresApp = lightColorScheme(
    primary = Principal,
    onPrimary = Blanco,
    primaryContainer = PrincipalClaro,
    onPrimaryContainer = Principal,
    secondary = PrincipalOscuro,
    onSecondary = Blanco,
    secondaryContainer = PrincipalClaro,
    onSecondaryContainer = Principal,
    tertiary = Acento,
    background = Fondo,
    onBackground = Texto,
    surface = Fondo,
    onSurface = Texto,
    surfaceVariant = PrincipalClaro,
    onSurfaceVariant = Gris,
    surfaceContainer = Blanco,
    surfaceContainerLow = Blanco,
    surfaceContainerHigh = Blanco,
    surfaceContainerHighest = Blanco,
    outline = Gris,
    outlineVariant = Borde,
    error = Rojo
)

// Siempre usamos el tema claro verde (también si el móvil está en modo oscuro)
@Composable
fun ListadeCompraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColoresApp,
        typography = Typography,
        content = content
    )
}