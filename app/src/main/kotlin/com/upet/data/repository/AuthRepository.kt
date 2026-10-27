package com.upet.data.repository

import android.util.Log
import com.upet.data.local.datastore.TokenDataStore
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.LoginRequest
import com.upet.data.remote.dto.RegisterClientRequest
import com.upet.data.remote.dto.RegisterWalkerRequest
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore,
    private val firebaseSessionRepository: FirebaseSessionRepository
) {

    suspend fun login(
        email: String,
        password: String
    ): Result<String> {

        return try {
            // 1. Limpiar cualquier sesión anterior para evitar
            // mezclar identidades de distintos usuarios.
            firebaseSessionRepository.signOut()
            tokenDataStore.clear()

            // 2. Autenticación normal contra Ktor.
            val response = api.login(
                LoginRequest(email, password)
            )

            val body = response.body()
                ?: throw IllegalStateException(
                    "El backend no devolvió datos de autenticación."
                )

            if (!response.isSuccessful || !body.success) {
                throw IllegalStateException(
                    "No fue posible iniciar sesión en UPet. " +
                            "HTTP ${response.code()}"
                )
            }

            // 3. Guardar temporalmente el JWT de Ktor.
            // El interceptor lo utilizará en la siguiente petición.
            tokenDataStore.saveToken(body.token)

            // 4. Solicitar el token Firebase y establecer la sesión.
            firebaseSessionRepository.signIn(
                expectedUserId = body.user.id
            )

            // 5. Guardar los datos del usuario solo cuando
            // ambas autenticaciones se hayan completado.
            tokenDataStore.saveUserId(body.user.id)
            tokenDataStore.saveUserType(body.user.isWalker)

            Log.d(
                "AuthRepository",
                "Sesión UPet y Firebase iniciada correctamente"
            )

            Result.success(body.token)

        } catch (e: Exception) {

            // Si falla cualquier parte del login,
            // evitamos dejar una sesión incompleta.
            firebaseSessionRepository.signOut()
            tokenDataStore.clear()

            Log.e(
                "AuthRepository",
                "Error de autenticación: ${e.message}"
            )

            Result.failure(e)
        }
    }

    suspend fun registerClient(
        name: String,
        email: String,
        password: String,
        phone: String,
        mainAddress: String
    ): Result<Unit> {
        return try {
            val request = RegisterClientRequest(
                name = name,
                email = email,
                password = password,
                phone = phone,
                mainAddress = mainAddress
            )
            val response = api.registerClient(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception("Registro fallido: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWalker(
        name: String,
        bio: String,
        email: String,
        password: String,
        phone: String,
        mainAddress: String,
        experience: String,
        serviceZoneLabel: String,
        serviceCenterLat: Double,
        serviceCenterLng: Double,
        zoneRadiusKm: Double,
        maxDogsPerWalk: Int
    ): Result<Unit> {
        return try {
            val request = RegisterWalkerRequest(
                name = name,
                bio = bio,
                email = email,
                password = password,
                phone = phone,
                mainAddress = mainAddress, // Corregido: DTO espera mainAddress
                experience = experience,
                serviceZoneLabel = serviceZoneLabel,
                serviceCenterLat = serviceCenterLat,
                serviceCenterLng = serviceCenterLng,
                zoneRadiusKm = zoneRadiusKm,
                maxDogsPerWalk = maxDogsPerWalk
            )
            val response = api.registerWalker(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception("Registro walker fallido: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
