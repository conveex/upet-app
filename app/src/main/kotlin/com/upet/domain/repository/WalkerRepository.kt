package com.upet.data.repository

import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.UpdateWalkerRequest
import com.upet.data.remote.dto.WalkerProfileResponse
import javax.inject.Inject

class WalkerRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getWalkerProfile(): Result<WalkerProfileResponse> {
        return try {
            val response = api.getMyWalkerProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener perfil de paseador: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWalkerProfile(
        bio: String,
        experience: String,
        serviceZoneLabel: String,
        maxDogs: Int,
        serviceCenterLat: Double,
        serviceCenterLng: Double,
        zoneRadiusKm: Double
    ): Result<WalkerProfileResponse> {
        return try {
            val request = UpdateWalkerRequest(
                bio = bio,
                experience = experience,
                serviceZoneLabel = serviceZoneLabel,
                maxDogs = maxDogs,
                serviceCenterLat = serviceCenterLat,
                serviceCenterLng = serviceCenterLng,
                zoneRadiusKm = zoneRadiusKm
            )
            val response = api.updateProfileWalker(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                 Result.failure(Exception("Error al actualizar el perfil: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
