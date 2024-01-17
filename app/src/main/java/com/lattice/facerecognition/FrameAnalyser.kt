package com.lattice.facerecognition

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.lattice.facerecognition.data.Prediction
import com.lattice.facerecognition.utils.BitmapUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt

class FrameAnalyser(
    private val context: Context,
    private var model: FaceNetModel
) : ImageAnalysis.Analyzer {

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()
    private val detector = FaceDetection.getClient(realTimeOpts)

    private val nameScoreHashmap = HashMap<String, ArrayList<Float>>()
    private var subject = FloatArray(model.embeddingDim)

    // Used to determine whether the incoming frame should be dropped or processed.
    private var isProcessing = false

    // Store the face embeddings in a ( String , FloatArray ) ArrayList.
    // Where String -> name of the person and FloatArray -> Embedding of the face.
    var faceList = ArrayList<Pair<String, FloatArray>>()

    private var t1: Long = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        if (isProcessing || faceList.size == 0) {
            image.close()
            return
        } else {
            isProcessing = true
            val cameraXImage = image.image!!
            var frameBitmap = Bitmap.createBitmap(
                cameraXImage.width,
                cameraXImage.height,
                Bitmap.Config.ARGB_8888
            )
            frameBitmap.copyPixelsFromBuffer(image.planes[0].buffer)
            frameBitmap =
                BitmapUtils.rotateBitmap(frameBitmap, image.imageInfo.rotationDegrees.toFloat())

            val inputImage = InputImage.fromBitmap(frameBitmap, 0)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    CoroutineScope(Dispatchers.Default).launch {
                        runModel(faces, frameBitmap)
                    }
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }


    private suspend fun runModel(faces: List<Face>, cameraFrameBitmap: Bitmap) {
        withContext(Dispatchers.Default) {
            t1 = System.currentTimeMillis()
            val predictions = ArrayList<Prediction>()
            for (face in faces) {
                try {
                    // Crop the frame using face.boundingBox.
                    // Convert the cropped Bitmap to a ByteBuffer.
                    // Finally, feed the ByteBuffer to the FaceNet model.
                    val croppedBitmap =
                        BitmapUtils.cropRectFromBitmap(cameraFrameBitmap, face.boundingBox)
                    subject = model.getFaceEmbedding(croppedBitmap)
                    for (i in 0 until faceList.size) {
                        // If this cluster ( i.e an ArrayList with a specific key ) does not exist,
                        // initialize a new one.
                        if (nameScoreHashmap[faceList[i].first] == null) {
                            // Compute the L2 norm and then append it to the ArrayList.
                            val p = ArrayList<Float>()
                            p.add(L2Norm(subject, faceList[i].second))
                            nameScoreHashmap[faceList[i].first] = p
                        }
                        // If this cluster exists, append the L2 norm/cosine score to it.
                        else {
                            nameScoreHashmap[faceList[i].first]?.add(
                                L2Norm(
                                    subject,
                                    faceList[i].second
                                )
                            )
                        }
                    }

                    // Compute the average of all scores norms for each cluster.
                    val avgScores = nameScoreHashmap.values.map { scores ->
                        scores.toFloatArray().average()
                    }
                    Log.d("UNCLE JII", "Average score for each user : $nameScoreHashmap")

                    val names = nameScoreHashmap.keys.toTypedArray()
                    nameScoreHashmap.clear()

                    // Calculate the minimum L2 distance from the stored average L2 norms.
                    val bestScoreUserName: String = // In case of L2 norm, choose the lowest value.
                        if (avgScores.minOrNull()!! > model.model.l2Threshold) {
                            "Unknown"
                        } else {
                            names[avgScores.indexOf(avgScores.minOrNull()!!)]
                        }

                    Log.d("UNCLE JII", "Person identified as $bestScoreUserName")
                    predictions.add(
                        Prediction(
                            face.boundingBox,
                            bestScoreUserName
                        )
                    )
                    (context.applicationContext as FaceRecognitionApp).nameDetected.emit(bestScoreUserName)

                } catch (e: Exception) {
                    // If any exception occurs with this box and continue with the next boxes.
                    Log.e("Model", "Exception in FrameAnalyser : ${e.message}")
                    continue
                }
                Log.e("Performance", "Inference time -> ${System.currentTimeMillis() - t1}")
            }
            withContext(Dispatchers.Main) {
                // Clear the BoundingBoxOverlay and set the new results ( boxes ) to be displayed.
                isProcessing = false
            }
        }
    }


    // Compute the L2 norm of ( x2 - x1 )
    private fun L2Norm(x1: FloatArray, x2: FloatArray): Float {
        return sqrt(x1.mapIndexed { i, xi -> (xi - x2[i]).pow(2) }.sum())
    }


}