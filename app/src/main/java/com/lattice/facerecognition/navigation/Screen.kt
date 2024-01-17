package com.lattice.facerecognition.navigation

sealed class Screen(val route: String) {
    data object LandingScreenRoute: Screen("landing_screen")
    data object AddPhotoScreenRoute: Screen("add_photo_screen")
    data object MarkAttendanceScreenRoute: Screen("make_attendance_screen")
}