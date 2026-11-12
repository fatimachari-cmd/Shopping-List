package com.fatimachari.listadecompra.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.fatimachari.listadecompra.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

// Se encarga de la cuenta del usuario: registro, login (correo y Google), nombre y salir
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {


    // Usuario actual (null si no ha iniciado sesión). Se actualiza solo.
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }


    // Iniciar sesión
    suspend fun loginWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("No se pudo obtener el usuario.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Crear cuenta nueva y guardar el nombre
    suspend fun registerWithEmail(nombre: String, email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("No se pudo obtener el usuario.")

            val cambios = UserProfileChangeRequest.Builder()
                .setDisplayName(nombre.trim())
                .build()
            user.updateProfile(cambios).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Iniciar sesión con Google
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)

            // Identificador de la app en Google (si falla el de los recursos, usamos este)
            val webClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (_: Exception) {
                "557488818859-u51sn6khk9549mldhbonaviap9me0sp8.apps.googleusercontent.com"
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResult = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = credentialResult.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                // Pasamos la cuenta de Google a Firebase
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)

                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: throw Exception("No se pudo iniciar sesión con Google.")
                Result.success(user)
            } else {
                Result.failure(Exception("Credencial de Google no válida."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Cambiar el nombre del usuario
    suspend fun updateName(nuevoNombre: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No hay usuario conectado.")

            val cambios = UserProfileChangeRequest.Builder()
                .setDisplayName(nuevoNombre.trim())
                .build()
            user.updateProfile(cambios).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Cerrar sesión
    fun signOut() {
        auth.signOut()
    }
}