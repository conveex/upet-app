package com.upet.presentation.navigation

sealed class UpetScreen(val route: String) {

    // Auth
    object Login : UpetScreen("login")
    object RegisterClient : UpetScreen("register_client")
    object RegisterWalker : UpetScreen("register_walker")

    // Homes
    object ClientHome : UpetScreen("client_home")
    object WalkerHome : UpetScreen("walker_home")

    //Profiles
    object Profile : UpetScreen("profile_client")
    object WalkerProfile : UpetScreen("profile_walker")
    object AddPet : UpetScreen("add_pet")

    //Walks
    object MyWalks : UpetScreen("active_walks")
    object RequestWalk : UpetScreen("request_walk")

    //Pet Details
        object PetDetail : UpetScreen("pet_detail/{petId}") {
            fun createRoute(petId: String) = "pet_detail/$petId"
        }


}
