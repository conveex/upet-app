package com.upet.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.upet.data.remote.ApiService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSessionRepository @Inject constructor(
    private val api: ApiService,
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun signIn(expectedUserId: String) {
        try {
            // 1. Solicitar Custom Token al backend Ktor.
            val response = api.getFirebaseCustomToken()

            if (!response.isSuccessful ||
                response.body()?.success != true
            ) {
                throw IllegalStateException(
                    "No fue posible obtener Firebase Custom Token. " +
                            "HTTP ${response.code()}"
                )
            }

            // 2. Extraer el Custom Token.
            val customToken = response.body()
                ?.firebaseCustomToken
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException(
                    "El backend no devolvió un Custom Token válido."
                )

            // 3. Iniciar sesión en Firebase Authentication.
            val authResult = firebaseAuth
                .signInWithCustomToken(customToken)
                .await()

            // 4. Comprobar que la identidad coincida
            // con el usuario autenticado en PostgreSQL.
            val firebaseUid = authResult.user?.uid

            if (firebaseUid != expectedUserId) {
                throw IllegalStateException(
                    "La identidad Firebase no coincide con la de UPet."
                )
            }

        } catch (e: Exception) {
            firebaseAuth.signOut()
            throw e
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}