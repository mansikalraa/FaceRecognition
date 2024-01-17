package com.lattice.facerecognition

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableSharedFlow

@HiltAndroidApp
class FaceRecognitionApp: Application() {

    internal var nameDetected = MutableSharedFlow<String>()
    override fun onCreate() {
        super.onCreate()
    }
}