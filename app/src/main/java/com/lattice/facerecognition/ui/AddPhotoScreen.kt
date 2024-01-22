package com.lattice.facerecognition.ui

import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.lattice.facerecognition.utils.GetCameraProvider.getCameraProvider
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun AddPhotoScreen(
    navController: NavController,
    viewModel: AddPhotoScreenViewModel = hiltViewModel()
) {
    val detector = FaceDetection.getClient()
    CameraPreview { imageBitmap ->
        val img = InputImage.fromBitmap(imageBitmap, 0)
        detector.process(img)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) Log.d("manseeyy", "no faces detected.")
                else {
                    for (face in faces) {
                        val croppedBitmap =
                            BitmapUtils.cropRectFromBitmap(imageBitmap, face.boundingBox)
                        viewModel.currentFaceBitmap = croppedBitmap
                        viewModel.showDialog = true
                    }
                }
            }
            .addOnFailureListener{ e ->
                e.localizedMessage?.let { Log.e("ERROR", it) }
            }
    }
    if (viewModel.showDialog) {
        RegisterFaceDialog(viewModel = viewModel)
    }
}


@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
    onCaptureButtonClick: (Bitmap) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    Box(modifier = Modifier.fillMaxSize()) {
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
                        // Set up the image capture use case
                        val imageFrameAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(480, 640))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()

                        imageFrameAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            // Convert ImageProxy to ImageBitmap for display
                            //imageBitmap = imageProxy.toBitmap()
                            val cameraXImage = imageProxy.image!!
                            imageBitmap = Bitmap.createBitmap(
                                cameraXImage.width,
                                cameraXImage.height,
                                Bitmap.Config.ARGB_8888
                            )
                            imageBitmap!!.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
                            imageBitmap =
                                BitmapUtils.rotateBitmap(
                                    imageBitmap!!,
                                    imageProxy.imageInfo.rotationDegrees.toFloat()
                                )

                            // Release the ImageProxy
                            imageProxy.close()
                        }
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageFrameAnalysis
                        )
                    } catch (ex: Exception) {
                        Log.e("manseeyy", "Use case binding failed", ex)
                    }
                }

                previewView
            }
        )
        Button(
            onClick = {
                onCaptureButtonClick(imageBitmap!!)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun RegisterFaceDialog(
    viewModel: AddPhotoScreenViewModel
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = {
                    // add photo embedding to facelist
                    viewModel.addPhoto(context.filesDir)
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = viewModel.currentFaceBitmap.asImageBitmap(),
                    contentDescription = "FACE_DETECTED",
                    modifier = Modifier.padding(10.dp)
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