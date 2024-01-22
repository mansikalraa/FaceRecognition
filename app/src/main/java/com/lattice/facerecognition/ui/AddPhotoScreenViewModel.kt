package com.lattice.facerecognition.ui

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import com.lattice.facerecognition.FaceNetModel
import com.lattice.facerecognition.FrameAnalyser
import com.lattice.facerecognition.data.sharedpreferences.PreferenceStorage
import com.lattice.facerecognition.ui.main.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import javax.inject.Inject

@HiltViewModel
class AddPhotoScreenViewModel @Inject constructor(
    private val frameAnalyser: FrameAnalyser,
    private val model: FaceNetModel,
    private val preferenceStorage: PreferenceStorage
) : ViewModel() {

    var showDialog by mutableStateOf(false)
    var faceName by mutableStateOf("")
    var currentFaceBitmap by mutableStateOf(createBitmap(10, 10))
    // add name and floatarray in facelist
    // call saveSerializedImageData with updated facelist

    internal fun addPhoto(filesDir: File) {
        val embedding = model.getFaceEmbedding(currentFaceBitmap)
        frameAnalyser.faceList.add(
            Pair(
                faceName,
                embedding
            )
        )
        saveSerializedImageData(filesDir, frameAnalyser.faceList)
    }

    private fun saveSerializedImageData(filesDir: File, data : ArrayList<Pair<String,FloatArray>> ) {
        val serializedDataFile = File( filesDir , "image_data" )
        if (serializedDataFile.exists()) serializedDataFile.delete()
        ObjectOutputStream( FileOutputStream( serializedDataFile )  ).apply {
            writeObject( data )
            flush()
            close()
        }
        preferenceStorage.serializeDataStored = true
    }
}