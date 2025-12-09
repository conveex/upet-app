package com.upet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// screens
import com.upet.ui.screens.auth.LoginScreen
import com.upet.ui.screens.auth.RegisterClientScreen
import com.upet.ui.screens.client.ClientHomeScreen
import com.upet.ui.screens.walker.WalkerHomeScreen
import com.upet.presentation.auth.register_walker.RegisterWalkerScreen
import com.upet.presentation.pets.AddPetScreen
import com.upet.presentation.profile.ClientProfileScreen

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
                onNavigateToAddPet = { navController.navigate(UpetScreen.AddPet.route) })
        }

        // HOME PASEADOR
        composable(UpetScreen.WalkerHome.route) {
            WalkerHomeScreen(onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToAvailableWalks = { navController.navigate("request_walk") },
                onNavigateToMyWalks = { navController.navigate("active_walks") },
                onNavigateToPredefinedRoutes = { navController.navigate("pending_walks") })
        }

        //CLIENT PROFILE
        composable(UpetScreen.Profile.route) {
            ClientProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(UpetScreen.Login.route) {
                        popUpTo(0) // limpia toda la pila
                    }
                }
            )
        }
        //ADD PET (con fallas)
        composable(UpetScreen.AddPet.route) {
            AddPetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}





