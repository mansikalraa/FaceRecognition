package com.lattice.facerecognition.ui

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import com.lattice.facerecognition.FrameAnalyser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddPhotoScreenViewModel @Inject constructor(
    private val frameAnalyser: FrameAnalyser
): ViewModel() {

    var showDialog by mutableStateOf(false)
    var faceName by mutableStateOf("")
    var currentFaceBitmap by mutableStateOf(createBitmap(10,10))
    // add name and floatarray in facelist
    // call saveSerializedImageData with updated facelist
}