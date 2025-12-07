package com.upet.presentation.navigation

sealed class UpetScreen(val route: String) {

    // Auth
    object Login : UpetScreen("login")
    object RegisterClient : UpetScreen("register_client")
    object RegisterWalker : UpetScreen("register_walker")

    // Homes
    object ClientHome : UpetScreen("client_home")
    object WalkerHome : UpetScreen("walker_home")
}
