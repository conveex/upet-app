package com.upet.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getLocationUpdates(intervalMs: Long = 5000L): Flow<LatLng> = callbackFlow {
        // Verificar permisos
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            close(Exception("Permisos de ubicación no concedidos"))
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(5f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(LatLng(location.latitude, location.longitude))
                }
            }
        }

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }


    suspend fun getCurrentLocation(): LatLng? {

        // Para las validaciones de proximidad del paseo
        // necesitamos permiso de ubicación precisa.
        val hasFinePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasFinePermission) {
            return null
        }

        val cancellationSource = CancellationTokenSource()

        return try {

            // Permite reutilizar una ubicación muy reciente
            // o solicitar una ubicación nueva del dispositivo.
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(5_000L)
                .setDurationMillis(15_000L)
                .build()

            // Evita que la operación quede esperando
            // indefinidamente si no hay señal GPS.
            val location = withTimeoutOrNull(17_000L) {
                client.getCurrentLocation(
                    request,
                    cancellationSource.token
                ).await()
            }

            if (location != null) {
                LatLng(
                    location.latitude,
                    location.longitude
                )
            } else {
                null
            }

        } catch (e: CancellationException) {
            throw e

        } catch (e: SecurityException) {
            null

        } catch (e: Exception) {
            null

        } finally {
            cancellationSource.cancel()
        }
    }
}
