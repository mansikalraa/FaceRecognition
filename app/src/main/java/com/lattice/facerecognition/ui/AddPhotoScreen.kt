package com.lattice.facerecognition.ui

import android.graphics.Bitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.lattice.facerecognition.utils.BitmapUtils
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun AddPhotoScreen(
    navController: NavController,
    viewModel: AddPhotoScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val previewView: PreviewView = remember { PreviewView(context) }
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val detector = FaceDetection.getClient()
    cameraController.bindToLifecycle(lifecycleOwner)
    cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
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
                        val pixelStride = imageProxy.planes[0].pixelStride
                        val rowStride = imageProxy.planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * imageProxy.width
                        var frameBitmap = Bitmap.createBitmap(
                            imageProxy.width,
                            imageProxy.height,
                            Bitmap.Config.ARGB_8888
                        )
                        frameBitmap.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
                        frameBitmap =
                            BitmapUtils.rotateBitmap(frameBitmap, imageProxy.imageInfo.rotationDegrees.toFloat())

                        val inputImage = InputImage.fromBitmap(frameBitmap, 0)
                        val img = InputImage.fromMediaImage(
                            image,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        detector.process(inputImage)
                            .addOnSuccessListener { faces ->
                                if (faces.isEmpty()) {
                                    // show snackbar
                                    Log.d("manseeyy", "no face")
                                } else {
                                    for (face in faces) {
                                        viewModel.currentFaceBitmap =
                                            BitmapUtils.cropRectFromBitmap(
                                                frameBitmap,
                                                face.boundingBox
                                            )
                                        viewModel.showDialog = true
                                        // show dialog with photo and name field
                                    }
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
    if (viewModel.showDialog){
        RegisterFaceDialog(viewModel = viewModel)
    }
}

@Composable
fun RegisterFaceDialog(
    viewModel: AddPhotoScreenViewModel
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = {
                    // add photo embedding to facelist
                    viewModel.faceName = ""
                    viewModel.currentFaceBitmap = createBitmap(10, 10)
                    viewModel.showDialog = false
                },
                enabled = viewModel.faceName.isNotBlank()
            ) {
                Text(text = "Add")
            }
        },
        text = {
            Column {
                Image(
                    painter = BitmapPainter(viewModel.currentFaceBitmap.asImageBitmap()),
                    contentDescription = "FACE_DETECTED"
                )
                OutlinedTextField(
                    value = viewModel.faceName,
                    onValueChange = { value ->
                        viewModel.faceName = value
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.faceName = ""
                viewModel.currentFaceBitmap = createBitmap(10, 10)
                viewModel.showDialog = false
            }) {
                Text(text = "Cancel")
            }
        }
    )
}