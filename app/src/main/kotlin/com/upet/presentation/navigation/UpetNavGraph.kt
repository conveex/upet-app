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
import com.upet.presentation.walks.WalkerWalksScreen // Para paseos disponibles
import com.upet.presentation.walks.WalkerActiveWalksScreen // Para paseos activos
import com.upet.presentation.walks.RequestWalkScreen
import com.upet.presentation.walks.PendingWalksScreen
import com.upet.presentation.walks.WalkDetailScreen
import com.upet.presentation.pets.PetDetailScreen
import com.upet.presentation.home_client.AddPaymentMethodScreen
import com.upet.presentation.home_client.PaymentMethodsScreen
import com.upet.presentation.home_walker.AddPaymentMethodWalkerScreen
import com.upet.presentation.home_walker.PaymentMethodsWalkerScreen
import com.upet.presentation.walks.ClientActiveWalksScreen
import com.upet.presentation.walks.ClientActiveWalkMapScreen
import com.upet.presentation.walks.WalkerActiveWalkMapScreen

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

        // REGISTRO PASEADOR
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
            ClientHomeScreen(
                onNavigateToProfile = { navController.navigate(UpetScreen.Profile.route) },
                onNavigateToRequestWalk = { navController.navigate("request_walk") },
                onNavigateToActiveWalks = { navController.navigate("client_active_walks") }, // Ruta específica para cliente
                onNavigateToPendingWalks = { navController.navigate("pending_walks") },
                onNavigateToAddPet = { navController.navigate(UpetScreen.AddPet.route) },
                onNavigateToPetDetail = { petId -> navController.navigate("pet_detail/$petId") }
            )
        }

        // HOME PASEADOR
        composable(UpetScreen.WalkerHome.route) {
            WalkerHomeScreen(
                onNavigateToProfile = { navController.navigate("profile_walker") },
                onNavigateToAvailableWalks = { navController.navigate("available_walks") }, // Paseos para aceptar
                onNavigateToMyWalks = { navController.navigate("walker_active_walks") } // Paseos ya aceptados/activos
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
        
        // PASEOS DISPONIBLES (WALKER)
        composable("available_walks") {
            WalkerWalksScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalkClick = { walkId ->
                    navController.navigate("walk_detail/$walkId?isAvailable=true")
                }
            )
        }
        
        // PASEOS ACTIVOS (WALKER)
        composable("walker_active_walks") {
            WalkerActiveWalksScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalkClick = { walkId ->
                    // Redirigir al mapa activo del paseador
                    navController.navigate("walker_active_map/$walkId")
                }
            )
        }
        
        //RequestWalk
        composable(UpetScreen.RequestWalk.route){
            RequestWalkScreen(
                onNavigateBack = { navController.popBackStack() },
                onPostWalk = {
                    navController.navigate("pending_walks") {
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
                    navController.navigate("payment_methods_walker") }, // Ruta específica para walker
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

        composable("add_payment_method") {
            AddPaymentMethodScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("payment_methods_walker") {
            PaymentMethodsWalkerScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddMethod = {
                    navController.navigate("add_payment_method_walker")
                }
            )
        }
        
        composable("add_payment_method_walker") {
            AddPaymentMethodWalkerScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // PASEOS PENDIENTES (CLIENTE)
        composable("pending_walks") {
            PendingWalksScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalkClick = { walkId ->
                    navController.navigate("walk_detail/$walkId")
                }
            )
        }
        
        // PASEOS ACTIVOS DEL CLIENTE
        composable("client_active_walks") {
             ClientActiveWalksScreen(
                 onNavigateBack = { navController.popBackStack() },
                 onWalkClick = { walkId ->
                     // Redirigir al mapa activo del cliente
                     navController.navigate("client_active_map/$walkId")
                 }
             )
        }

        // DETALLE PASEO (Genérico / Pendiente / Disponible)
        composable(
            route = "walk_detail/{walkId}?isAvailable={isAvailable}",
            arguments = listOf(
                navArgument("walkId") { type = NavType.StringType },
                navArgument("isAvailable") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val walkId = backStackEntry.arguments?.getString("walkId") ?: return@composable
            val isAvailable = backStackEntry.arguments?.getBoolean("isAvailable") ?: false
            
            WalkDetailScreen(
                walkId = walkId,
                isAvailable = isAvailable,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // MAPA ACTIVO (CLIENTE)
        composable(
            route = "client_active_map/{walkId}",
            arguments = listOf(navArgument("walkId") { type = NavType.StringType })
        ) {
            ClientActiveWalkMapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // MAPA ACTIVO (PASEADOR)
        composable(
            route = "walker_active_map/{walkId}",
            arguments = listOf(navArgument("walkId") { type = NavType.StringType })
        ) {
            WalkerActiveWalkMapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
