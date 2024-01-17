package com.lattice.facerecognition.ui

import android.util.Log
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lattice.facerecognition.utils.GetCameraProvider.getCameraProvider
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun MarkAttendanceScreen(
    navController: NavController,
    viewModel: MarkAttendanceViewModel = hiltViewModel()
) {
    Box(modifier = Modifier.fillMaxSize()){
        CameraPreview(viewModel)
    }
}

@Composable
fun CameraPreview(
    viewModel: MarkAttendanceViewModel,
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // CameraX Preview UseCase
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            coroutineScope.launch {
                val cameraProvider = context.getCameraProvider()
                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()
                    val imageFrameAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size( 480, 640 ) )
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                    imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), viewModel.frameAnalyser )
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, previewUseCase, imageFrameAnalysis
                    )
                } catch (ex: Exception) {
                    Log.e("manseeyy", "Use case binding failed", ex)
                }
            }

            previewView
        }
    )
}
