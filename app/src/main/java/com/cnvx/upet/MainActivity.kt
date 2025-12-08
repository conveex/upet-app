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
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenStore: TokenDataStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            UpetNavGraph(navController)
            val token = tokenStore.token.collectAsState(initial = null).value
            val role = tokenStore.role.collectAsState(initial = null).value

            LaunchedEffect(token) {
                if (token != null && role != null) {
                    if (role == "client") {
                        navController.navigate(UpetScreen.ClientHome.route)
                    } else {
                        navController.navigate(UpetScreen.WalkerHome.route)
                    }
                }
            }



        }
    }
}



