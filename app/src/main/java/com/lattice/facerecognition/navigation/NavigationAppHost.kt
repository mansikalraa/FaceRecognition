package com.lattice.facerecognition.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lattice.facerecognition.ui.AddPhotoScreen
import com.lattice.facerecognition.ui.LandingScreen
import com.lattice.facerecognition.ui.MarkAttendanceScreen
import com.lattice.facerecognition.ui.RegisteredNamesScreen

@Composable
fun NavigationAppHost(navController: NavController) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = Screen.LandingScreenRoute.route,
        builder = {
            composable(Screen.LandingScreenRoute.route) { LandingScreen(navController)}
            composable(Screen.AddPhotoScreenRoute.route) { AddPhotoScreen(navController) }
            composable(Screen.MarkAttendanceScreenRoute.route) { MarkAttendanceScreen(navController) }
            composable(Screen.RegisteredFaceScreenRoute.route) { RegisteredNamesScreen(navController) }
        })
}