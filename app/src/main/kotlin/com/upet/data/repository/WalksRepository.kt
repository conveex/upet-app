package com.upet.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.AvailableWalkDto
import com.upet.data.remote.dto.CalculateRouteRequestDto
import com.upet.data.remote.dto.CreateWalkRequest
import com.upet.data.remote.dto.LatLngDto
import com.upet.data.remote.dto.PendingWalkDto
import com.upet.data.remote.dto.WalkDetailDto
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

        // Formato ISO-8601 estricto (YYYY-MM-DDTHH:MM:SS) sin milisegundos si es posible
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(ZoneOffset.UTC)

        val startTime = requestedStartTime ?: formatter.format(Instant.now().plus(10, ChronoUnit.MINUTES))

        val originDto = LatLngDto(origin.latitude, origin.longitude)
        val destinationDto = destination?.let { LatLngDto(it.latitude, it.longitude) }

        // Asumimos pickup = origen y dropoff = destino (o origen si es circular)
        val pickupDto = originDto
        val dropoffDto = destinationDto ?: originDto
        
        // Aseguramos que distancia y duracion sean > 0
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

        Log.d("RequestWalk", "Creating walk with request: $request")

        try {
            val response = api.createWalk(request)
            if (response.isSuccessful && response.body()?.success == true) {
                // Ahora accedemos a walk.id
                return Result.success(response.body()!!.walk.id)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RequestWalk", "Error crear paseo: ${response.code()} - $errorBody")
                return Result.failure(Exception("Error al crear paseo (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("RequestWalk", "Exception crear paseo", e)
            return Result.failure(e)
        }
    }

    suspend fun getPendingWalks(): Result<List<PendingWalkDto>> {
        try {
            val response = api.getClientPendingWalks()
            if (response.isSuccessful && response.body()?.success == true) {
                return Result.success(response.body()!!.walks)
            } else {
                return Result.failure(Exception("Error al obtener paseos pendientes: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getWalkDetail(walkId: String): Result<WalkDetailDto> {
        try {
            val response = api.getWalkDetail(walkId)
            if (response.isSuccessful && response.body()?.success == true) {
                return Result.success(response.body()!!.walk)
            } else {
                return Result.failure(Exception("Error al obtener detalle del paseo: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun cancelWalk(walkId: String): Result<Boolean> {
        try {
            val response = api.cancelWalk(walkId)
            if (response.isSuccessful && response.body()?.success == true) {
                return Result.success(true)
            } else {
                return Result.failure(Exception("Error al cancelar el paseo: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getAvailableWalks(): Result<List<AvailableWalkDto>> {
        try {
            val response = api.getAvailableWalks()
            if (response.isSuccessful && response.body()?.success == true) {
                return Result.success(response.body()!!.walks)
            } else {
                return Result.failure(Exception("Error al obtener paseos disponibles: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
