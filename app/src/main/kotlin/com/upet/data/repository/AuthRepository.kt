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
    private val tokenDataStore: TokenDataStore
) {

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                tokenDataStore.saveToken(body.token)
                tokenDataStore.saveUserId(body.user.id)
                tokenDataStore.saveUserType(body.user.isWalker) // Este método debe estar en TokenDataStore

                Log.d("AuthRepository", "Login success, token saved. UserID: ${body.user.id}")
                Result.success(body.token)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception("Login fallido: $errorMsg"))
            }
        } catch (e: Exception) {
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
