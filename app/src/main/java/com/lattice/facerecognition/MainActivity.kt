package com.lattice.facerecognition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.lattice.facerecognition.navigation.NavigationAppHost
import com.lattice.facerecognition.ui.theme.FaceRecognitionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FaceRecognitionTheme {
                val navComposable = rememberNavController()
                NavigationAppHost(navController = navComposable)
            }
        }
    }
}