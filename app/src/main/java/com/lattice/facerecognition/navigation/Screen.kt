package com.lattice.facerecognition.navigation

sealed class Screen(val route: String) {
    data object LandingScreenRoute: Screen("landing_screen")
}