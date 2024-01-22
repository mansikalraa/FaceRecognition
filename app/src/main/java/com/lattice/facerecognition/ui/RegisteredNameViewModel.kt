package com.lattice.facerecognition.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lattice.facerecognition.FrameAnalyser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisteredNameViewModel @Inject constructor(
    frameAnalyzer: FrameAnalyser
) : ViewModel() {
    var namesList by mutableStateOf(listOf<String>())

    init {
        frameAnalyzer.faceList.forEach { pair ->
            namesList = namesList + listOf(pair.first)
        }
    }
}