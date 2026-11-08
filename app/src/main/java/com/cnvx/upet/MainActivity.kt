package com.cnvx.upet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.cnvx.upet.ui.theme.UPetTheme              // ← NUEVO
import com.upet.data.local.datastore.TokenDataStore
import com.upet.data.remote.ApiService
import com.upet.data.remote.dto.UpdateFcmTokenRequest
import com.upet.presentation.navigation.UpetNavGraph
import com.upet.presentation.navigation.UpetScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStore: TokenDataStore

    @Inject
    lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchAndSendFcmToken()

        setContent {
            UPetTheme {                              // ✅ envuelve todo el árbol
                val navController = rememberNavController()
                val token = runBlocking { tokenStore.getToken() }
                val role = runBlocking { tokenStore.getRole() }

                LaunchedEffect(Unit) {
                    if (token.isNullOrEmpty()) {
                        navController.navigate(UpetScreen.Login.route) {
                            popUpTo(0)
                        }
                    } else {
                        if (role == "client") {
                            navController.navigate(UpetScreen.ClientHome.route) {
                                popUpTo(0)
                            }
                        } else {
                            navController.navigate(UpetScreen.WalkerHome.route) {
                                popUpTo(0)
                            }
                        }
                    }
                }

                UpetNavGraph(navController)
            }
        }
    }

    private fun fetchAndSendFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "Token actual: $token")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = tokenStore.getUserId()
                    if (!userId.isNullOrEmpty()) {
                        val response = api.updateFcmToken(UpdateFcmTokenRequest(token))
                        if (response.isSuccessful && response.body()?.success == true) {
                            Log.d("FCM", "Token enviado al backend en onCreate")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "Error enviando token en onCreate", e)
                }
            }
        })
    }
}