package com.upet.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.upet.data.repository.LocationTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val walkId = intent.getStringExtra("walkId") ?: return START_NOT_STICKY
                start(walkId)
            }
            ACTION_STOP -> stop()
        }
        return START_NOT_STICKY
    }

    private fun start(walkId: String) {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Paseo en curso...")
            .setContentText("Ubicación: rastreando...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Considera reemplazarlo por un icono de tu app
            .setOngoing(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("location", "Ubicación en Tiempo Real", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        locationTracker.getLocationUpdates(10000L) // Actualizaciones cada 10 segundos
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val updatedNotification = notification.setContentText(
                    "Última ubicación: ${location.latitude}, ${location.longitude}"
                )
                notificationManager.notify(1, updatedNotification.build())

                val locationData = hashMapOf(
                    "lat" to location.latitude,
                    "lng" to location.longitude,
                    "timestampMillis" to System.currentTimeMillis()
                )
                firestore.collection("tracking").document(walkId).collection("latest").document("latest")
                    .set(locationData)
                    .addOnFailureListener { e -> e.printStackTrace() }
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
