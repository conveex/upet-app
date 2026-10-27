
package com.upet.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.upet.data.repository.LocationTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var locationTracker: LocationTracker

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private var trackingJob: Job? = null
    private var activeWalkId: String? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        private const val CHANNEL_ID = "location"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "UPET_TRACKING"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_START -> {
                val walkId = intent.getStringExtra("walkId")

                if (walkId.isNullOrBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }

                startTracking(walkId)
            }

            ACTION_STOP -> stopTracking()
        }

        return START_NOT_STICKY
    }

    private fun startTracking(walkId: String) {

        // Evitar iniciar dos recopilaciones
        // de GPS para el mismo paseo.
        if (
            activeWalkId == walkId &&
            trackingJob?.isActive == true
        ) {
            return
        }

        trackingJob?.cancel()
        activeWalkId = walkId

        createNotificationChannel()

        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("UPet - Paseo en curso")
            .setContentText("Compartiendo ubicación del paseo")
            .setSmallIcon(
                android.R.drawable.ic_menu_mylocation
            )
            .setOngoing(true)
            .build()

        // Convertir el servicio a primer plano
        // antes de iniciar la recopilación GPS.
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )

        Log.d(TAG, "Tracking iniciado para paseo: $walkId")

        trackingJob = locationTracker
            .getLocationUpdates(10_000L)
            .onEach { location ->

                val timestampMillis =
                    System.currentTimeMillis()

                val walkRef = firestore
                    .collection("tracking")
                    .document(walkId)

                // Documento nuevo para historial.
                val pointRef = walkRef
                    .collection("points")
                    .document()

                val pointData = hashMapOf<String, Any>(
                    "lat" to location.latitude,
                    "lng" to location.longitude,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "timestampMillis" to timestampMillis,
                    "is_manual" to false
                )

                // Documento de última ubicación.
                val latestRef = walkRef
                    .collection("latest")
                    .document("latest")

                val latestData = hashMapOf<String, Any>(
                    "lat" to location.latitude,
                    "lng" to location.longitude,
                    "timestampMillis" to timestampMillis
                )

                // Ambas escrituras en una operación.
                val batch = firestore.batch()

                batch.set(pointRef, pointData)
                batch.set(latestRef, latestData)

                batch.commit()
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "Punto de tracking sincronizado"
                        )
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            TAG,
                            "Error guardando ubicación",
                            error
                        )
                    }
            }
            .catch { error ->
                Log.e(
                    TAG,
                    "Error obteniendo ubicación GPS",
                    error
                )
            }
            .launchIn(serviceScope)
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ubicación en Tiempo Real",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    private fun stopTracking() {

        trackingJob?.cancel()
        trackingJob = null
        activeWalkId = null

        Log.d(TAG, "Tracking detenido")

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        trackingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}