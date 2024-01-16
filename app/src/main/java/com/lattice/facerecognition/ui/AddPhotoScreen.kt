package com.lattice.facerecognition.ui

import android.util.AttributeSet
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.lattice.facerecognition.BoundingBoxOverlay
import com.lattice.facerecognition.FaceNetModel
import com.lattice.facerecognition.FrameAnalyser
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun AddPhotoScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val previewView: PreviewView = remember { PreviewView(context) }
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val detector = FaceDetection.getClient()
    cameraController.bindToLifecycle(lifecycleOwner)
    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    previewView.controller = cameraController

    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        IconButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            onClick = {
                cameraController.setImageAnalysisAnalyzer(executor) { imageProxy ->
                    imageProxy.image?.let { image ->
                        val img = InputImage.fromMediaImage(
                            image,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        detector.process(img)
                            .addOnSuccessListener { faces ->
                                if (faces.isNotEmpty()) {
                                    Log.d("manseeyy", "${faces.size} faces detected")
                                } else {
                                    Log.d("manseeyy", "no faces detected")
                                }
                            }
                            .addOnFailureListener { e ->
                                e.localizedMessage?.let { Log.e("ERROR", it) }
                            }
                    }
                }
            }
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }
    }
}

