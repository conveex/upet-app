package com.upet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

// screens
import com.upet.ui.screens.auth.LoginScreen
import com.upet.ui.screens.auth.RegisterClientScreen
import com.upet.ui.screens.client.ClientHomeScreen
import com.upet.ui.screens.walker.WalkerHomeScreen
import com.upet.presentation.auth.register_walker.RegisterWalkerScreen
import com.upet.presentation.pets.AddPetScreen
import com.upet.presentation.profile.ClientProfileScreen
import com.upet.presentation.profile.WalkerProfileScreen
import com.upet.presentation.walks.WalkerWalksScreen // Corregido import
import com.upet.presentation.walks.RequestWalkScreen
import com.upet.presentation.walks.PendingWalksScreen
import com.upet.presentation.walks.WalkDetailScreen
import com.upet.presentation.pets.PetDetailScreen
import com.upet.presentation.home_client.AddPaymentMethodScreen
import com.upet.presentation.home_client.PaymentMethodsScreen
import com.upet.presentation.home_walker.AddPaymentMethodWalkerScreen
import com.upet.presentation.home_walker.PaymentMethodsWalkerScreen

@Composable
fun UpetNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = UpetScreen.Login.route
    ) {

        // LOGIN
        composable(UpetScreen.Login.route) {
            LoginScreen(
                onLoginSuccessClient = {
                    navController.navigate(UpetScreen.ClientHome.route) {
                        popUpTo(UpetScreen.Login.route) { inclusive = true }
                    }
                },
                onLoginSuccessWalker = {
                    navController.navigate(UpetScreen.WalkerHome.route) {
                        popUpTo(UpetScreen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegisterClient = {
                    navController.navigate(UpetScreen.RegisterClient.route)
                },
                onNavigateToRegisterWalker = {
                    navController.navigate(UpetScreen.RegisterWalker.route)
                }
            )
        }

        // REGISTRO CLIENTE
        composable(UpetScreen.RegisterClient.route) {
            RegisterClientScreen(
                onRegisterSuccess = {
                    navController.navigate(UpetScreen.Login.route) {
                        popUpTo(UpetScreen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // REGISTRO PASEADOR (cuando lo tengas)
        composable(UpetScreen.RegisterWalker.route) {
            RegisterWalkerScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // HOME CLIENTE
        composable(UpetScreen.ClientHome.route) {
            ClientHomeScreen(onNavigateToProfile = {
                navController.navigate(UpetScreen.Profile.route) },
                onNavigateToRequestWalk = { navController.navigate("request_walk") },
                onNavigateToActiveWalks = { navController.navigate("active_walks") },
                onNavigateToPendingWalks = { navController.navigate("pending_walks") },
                onNavigateToAddPet = { navController.navigate(UpetScreen.AddPet.route) },
                onNavigateToPetDetail = {petId -> navController.navigate("pet_detail/$petId")})
        }

        // HOME PASEADOR
        composable(UpetScreen.WalkerHome.route) {
            WalkerHomeScreen(
                onNavigateToProfile = { navController.navigate("profile_walker") },
                // Corregido: Available Walks va a MyWalks (WalkerWalksScreen)
                onNavigateToAvailableWalks = { navController.navigate(UpetScreen.MyWalks.route) }, 
                onNavigateToMyWalks = { navController.navigate("active_walks") },
                onNavigateToPredefinedRoutes = { navController.navigate("pending_walks") }
            )
        }

        //CLIENT PROFILE
        composable(UpetScreen.Profile.route) {
            ClientProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPaymentMethods = {
                    navController.navigate("payment_methods") },
                onLogout = {
                    navController.navigate(UpetScreen.Login.route) {
                        popUpTo(0) // limpia toda la pila
                    }
                }
            )
        }
        //ADD PET
        composable(UpetScreen.AddPet.route) {
            AddPetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Walks Walker (Available Walks)
        composable(UpetScreen.MyWalks.route) {
            WalkerWalksScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalkClick = { walkId ->
                    navController.navigate("walk_detail/$walkId")
                }
            )
        }
        
        //RequestWalk
        composable(UpetScreen.RequestWalk.route){
            RequestWalkScreen(
                onNavigateBack = { navController.popBackStack() },
                onPostWalk = {
                    navController.navigate("pending_walks") {
                        // Opcional: limpiar backstack para no volver al request
                        popUpTo(UpetScreen.ClientHome.route)
                    }
                }
            )
        }
        //WalkerProfile
        composable(UpetScreen.WalkerProfile.route){
            WalkerProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPaymentMethods = {
                    navController.navigate("payment_methods") },
                onLogout = {
                    navController.navigate(UpetScreen.Login.route) {
                        popUpTo(0) // limpia toda la pila
                    }
                }
            )
        }
        //Detalles de mascota
        composable(
            route = "pet_detail/{petId}",
            arguments = listOf(navArgument("petId") { type = NavType.StringType })
        ) {
            PetDetailScreen(
                petId = it.arguments?.getString("petId")!!,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("payment_methods") {
            PaymentMethodsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddMethod = {
                    navController.navigate("add_payment_method")
                }
            )
        }

        // Nota: hay rutas duplicadas para walkers, idealmente usar IDs distintos
        // pero lo dejo como estaba por ahora para no romper nada.

        composable("add_payment_method") {
            AddPaymentMethodScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // NUEVA PANTALLA: PASEOS PENDIENTES
        composable("pending_walks") {
            PendingWalksScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalkClick = { walkId ->
                    navController.navigate("walk_detail/$walkId")
                }
            )
        }

        // DETALLE PASEO
        composable(
            route = "walk_detail/{walkId}",
            arguments = listOf(navArgument("walkId") { type = NavType.StringType })
        ) {
            val walkId = it.arguments?.getString("walkId") ?: return@composable
            WalkDetailScreen(
                walkId = walkId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
