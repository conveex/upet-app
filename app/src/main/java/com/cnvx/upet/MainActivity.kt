package com.cnvx.upet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.rememberNavController
import com.upet.data.local.datastore.TokenDataStore
import com.upet.presentation.navigation.UpetNavGraph
import com.upet.presentation.navigation.UpetScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStore: TokenDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()
            val token = runBlocking { tokenStore.getToken() }
            val role = runBlocking { tokenStore.getRole() }

            LaunchedEffect(Unit) {
                if (token.isNullOrEmpty()) {
                    // No hay sesión → ir al login
                    navController.navigate(UpetScreen.Login.route) {
                        popUpTo(0)
                    }
                } else {
                    // Hay token → entrar según rol
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



