package com.fatimachari.listadecompra.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.fatimachari.listadecompra.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Clase encargada de gestionar la autenticación con Firebase (Email/Contraseña y Google).
 * Puedes llamar a estas funciones desde tu propio ViewModel o pantalla UI.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    /**
     * Iniciar sesión con Email y Contraseña.
     */
    suspend fun loginWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("No se pudo obtener la información del usuario.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registrar nuevo usuario con Email y Contraseña.
     */
    suspend fun registerWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("No se pudo obtener la información del usuario.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Iniciar sesión con Google usando Credential Manager.
     */
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
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
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential =
                    GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: throw Exception("No se pudo iniciar sesión con Google.")
                Result.success(user)
            } else {
                Result.failure(Exception("Tipo de credencial de Google no válido."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cerrar la sesión actual.
     */
    fun signOut() {
        auth.signOut()
    }
}
