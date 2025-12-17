package com.upet.data.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.upet.data.local.datastore.TokenDataStore
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.UpdateFcmTokenRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var tokenStore: TokenDataStore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token generado: $token")
        
        // Enviamos el token al backend
        sendTokenToBackend(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Manejo de notificaciones en primer plano si es necesario
        Log.d("FCM", "Mensaje recibido de: ${remoteMessage.from}")
        
        remoteMessage.notification?.let {
            Log.d("FCM", "Título: ${it.title}, Cuerpo: ${it.body}")
            // Aquí podrías mostrar una notificación local si la app está abierta
        }
    }

    private fun sendTokenToBackend(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Verificamos si hay un usuario logueado antes de enviar
                val userId = tokenStore.getUserId()
                if (userId != null) {
                    val response = api.updateFcmToken(UpdateFcmTokenRequest(token))
                    if (response.isSuccessful && response.body()?.success == true) {
                        Log.d("FCM", "Token actualizado en backend exitosamente")
                    } else {
                        Log.e("FCM", "Error al actualizar token en backend: ${response.code()}")
                    }
                } else {
                    Log.d("FCM", "No hay usuario logueado, token guardado para después")
                    // Podrías guardarlo en DataStore temporalmente para enviarlo al hacer login
                }
            } catch (e: Exception) {
                Log.e("FCM", "Excepción enviando token: ${e.message}")
            }
        }
    }
}
