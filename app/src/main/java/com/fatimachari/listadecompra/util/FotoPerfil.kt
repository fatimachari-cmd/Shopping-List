package com.fatimachari.listadecompra.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

fun uriABase64(context: Context, uri: Uri): String? {
    return try {
        val flujo = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(flujo)
        flujo.close()
        if (original == null) return null

        // Recorte cuadrado desde el centro
        val lado = minOf(original.width, original.height)
        val x = (original.width - lado) / 2
        val y = (original.height - lado) / 2
        val cuadrada = Bitmap.createBitmap(original, x, y, lado, lado)
        val pequena = Bitmap.createScaledBitmap(cuadrada, 256, 256, true)

        val salida = ByteArrayOutputStream()
        pequena.compress(Bitmap.CompressFormat.JPEG, 70, salida)
        Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}

// Del texto guardado en Firestore otra vez a imagen
fun base64ABitmap(texto: String): Bitmap? {
    if (texto.isBlank()) return null
    return try {
        val bytes = Base64.decode(texto, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}