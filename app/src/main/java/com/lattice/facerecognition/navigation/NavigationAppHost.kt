package com.lattice.facerecognition.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lattice.facerecognition.ui.LandingScreen

@Composable
fun NavigationAppHost(navController: NavController) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = Screen.LandingScreenRoute.route,
        builder = {
            composable(Screen.LandingScreenRoute.route) { LandingScreen(navController)}
        })
}