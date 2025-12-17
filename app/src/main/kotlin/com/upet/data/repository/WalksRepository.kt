package com.upet.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.AcceptWalkRequest
import com.upet.data.remote.dto.CalculateRouteRequestDto
import com.upet.data.remote.dto.CreateWalkRequest
import com.upet.data.remote.dto.EndWalkRequest
import com.upet.data.remote.dto.LatLngDto
import com.upet.data.remote.dto.StartWalkRequest
import com.upet.data.remote.dto.WalkDetailDto
import com.upet.data.remote.dto.WalkSummaryDto
import com.upet.data.remote.dto.WalkType
import com.upet.presentation.walks.RouteOptionUi
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.ZoneOffset
import javax.inject.Inject

class WalksRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun calculateRoute(
        type: WalkType,
        origin: LatLng,
        destination: LatLng?,
        timeMinutes: Int?,
        distanceKm: Double?
    ): Result<List<RouteOptionUi>> {

        val requestDto = CalculateRouteRequestDto(
            type = type, // A_TO_B, TIME, DISTANCE
            origin = LatLngDto(origin.latitude, origin.longitude),
            destination = destination?.let {
                LatLngDto(it.latitude, it.longitude)
            },
            timeMinutes = timeMinutes,
            distanceKm = distanceKm
        )
        Log.d("RequestWalk", "Request DTO = $requestDto")

        val response = api.calculateRoute(requestDto)

        if (!response.isSuccessful) {
            return Result.failure(Exception("Backend error ${response.code()}"))
        }

        val body = response.body() ?: return Result.failure(Exception("Respuesta vacía del servidor"))

        val routes = body.routes.map {
            RouteOptionUi(
                polylineEncoded = it.polylineEncoded,
                points = PolyUtil.decode(it.polylineEncoded),
                distanceKm = it.distanceKm,
                durationMin = it.durationMin,
                price = it.priceAmount
            )
        }

        return Result.success(routes)
    }

    suspend fun createWalk(
        petIds: List<String>,
        paymentMethodIds: List<String>,
        walkType: WalkType,
        origin: LatLng,
        destination: LatLng?,
        distanceMeters: Int,
        durationSeconds: Int,
        polylineEncoded: String,
        requestedStartTime: String? = null // ISO string
    ): Result<String> {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.UTC)
        val startTime = requestedStartTime ?: formatter.format(Instant.now().plus(10, ChronoUnit.MINUTES))
        val originDto = LatLngDto(origin.latitude, origin.longitude)
        val destinationDto = destination?.let { LatLngDto(it.latitude, it.longitude) }
        val pickupDto = originDto
        val dropoffDto = destinationDto ?: originDto
        val finalDistance = if (distanceMeters > 0) distanceMeters else 100
        val finalDuration = if (durationSeconds > 0) durationSeconds else 60

        val request = CreateWalkRequest(
            type = walkType.name,
            origin = originDto,
            destination = destinationDto,
            pickup = pickupDto,
            dropoff = dropoffDto,
            estimatedDistanceMeters = finalDistance,
            estimatedDurationSeconds = finalDuration,
            selectedRoutePolylineEncoded = polylineEncoded,
            requestedStartTime = startTime,
            predefinedRouteId = null,
            petIds = petIds,
            paymentMethodIds = paymentMethodIds
        )

        try {
            val response = api.createWalk(request)
            if (response.isSuccessful && response.body()?.success == true) {
                return Result.success(response.body()!!.walk.id)
            } else {
                val errorBody = response.errorBody()?.string()
                return Result.failure(Exception("Error al crear paseo (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getClientPendingWalks(): Result<List<WalkSummaryDto>> {
        return try {
            val response = api.getClientPendingWalks()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walks)
            } else {
                Result.failure(Exception("Error al obtener paseos pendientes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getClientActiveWalks(): Result<List<WalkSummaryDto>> {
        return try {
            val response = api.getClientActiveWalks()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walks)
            } else {
                Result.failure(Exception("Error al obtener paseos activos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWalkerActiveWalks(): Result<List<WalkSummaryDto>> {
        return try {
            val response = api.getWalkerActiveWalks()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walks)
            } else {
                Result.failure(Exception("Error al obtener paseos de paseador: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWalkDetail(walkId: String): Result<WalkDetailDto> {
        return try {
            val response = api.getWalkDetail(walkId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walk)
            } else {
                Result.failure(Exception("Error al obtener detalle del paseo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAvailableWalkDetail(walkId: String): Result<WalkDetailDto> {
        return try {
            val response = api.getAvailableWalkDetail(walkId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walk)
            } else {
                Result.failure(Exception("Error al obtener detalle de paseo disponible: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun cancelWalk(walkId: String): Result<Boolean> {
        return try {
            val response = api.cancelWalk(walkId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al cancelar el paseo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableWalks(): Result<List<WalkSummaryDto>> {
        return try {
            val response = api.getAvailableWalks()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walks)
            } else {
                Result.failure(Exception("Error al obtener paseos disponibles: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptWalk(walkId: String, agreedPaymentMethodId: String): Result<WalkDetailDto> {
        return try {
            val request = AcceptWalkRequest(agreedPaymentMethodId = agreedPaymentMethodId)
            val response = api.acceptWalk(walkId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walk)
            } else {
                Result.failure(Exception("Error al aceptar el paseo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun startWalk(walkId: String, lat: Double, lng: Double): Result<WalkDetailDto> {
        return try {
            val request = StartWalkRequest(lat, lng)
            val response = api.startWalk(walkId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walk)
            } else {
                Result.failure(Exception("Error al iniciar el paseo: ${response.code()}"))
            }
        } catch(e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun endWalk(walkId: String, lat: Double, lng: Double): Result<WalkDetailDto> {
        return try {
            val request = EndWalkRequest(lat, lng)
            val response = api.endWalk(walkId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.walk)
            } else {
                Result.failure(Exception("Error al finalizar el paseo: ${response.code()}"))
            }
        } catch(e: Exception) {
            Result.failure(e)
        }
    }
}
