package com.lattice.facerecognition.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.lattice.facerecognition.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(navController: NavController) {
    val context = LocalContext.current
    var index by remember {
        mutableIntStateOf(1)
    }
    val requestCameraPermission = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { granted ->
        if (granted) {
            when (index){
                1 -> navController.navigate(Screen.AddPhotoScreenRoute.route)
                2 -> navController.navigate(Screen.MarkAttendanceScreenRoute.route)
            }
        } else {
            Log.d("manseeyy", "permission denied")
        }
    })
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Face Recognition System") })
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            index = 1
                            requestCameraPermission.launch(Manifest.permission.CAMERA)
                        } else {
                            navController.navigate(Screen.AddPhotoScreenRoute.route)
                        }
                    }) {
                        Text(text = "Add photo")
                    }
                    Button(onClick = {
                        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            index = 2
                            requestCameraPermission.launch(Manifest.permission.CAMERA)
                        } else {
                            navController.navigate(Screen.MarkAttendanceScreenRoute.route)
                        }
                    }) {
                        Text(text = "Mark attendance")
                    }
                }
            }
        }
    )
}