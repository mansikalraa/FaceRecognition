package com.lattice.facerecognition.ui

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.ViewModel
import com.lattice.facerecognition.FrameAnalyser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MarkAttendanceViewModel@Inject constructor(
    frameAnalyser: FrameAnalyser
): ViewModel() {
    val frameAnalyser: ImageAnalysis.Analyzer = frameAnalyser
}