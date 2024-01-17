package com.lattice.facerecognition.ui.main

import android.content.Intent
import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.compose.rememberNavController
import com.lattice.facerecognition.FaceNetModel
import com.lattice.facerecognition.FileReader
import com.lattice.facerecognition.FrameAnalyser
import com.lattice.facerecognition.data.sharedpreferences.PreferenceStorage
import com.lattice.facerecognition.navigation.NavigationAppHost
import com.lattice.facerecognition.ui.theme.FaceRecognitionTheme
import com.lattice.facerecognition.utils.BitmapUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var faceNetModel: FaceNetModel
    @Inject lateinit var frameAnalyser: FrameAnalyser
    @Inject lateinit var fileReader: FileReader
    @Inject lateinit var preferenceStorage: PreferenceStorage

    private val mainViewModel by viewModels<MainViewModel>()

    private val fileReaderCallback = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(data: ArrayList<Pair<String, FloatArray>>, numImagesWithNoFaces: Int) {
            frameAnalyser.faceList = data
            mainViewModel.saveSerializedImageData( filesDir, data )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(preferenceStorage.serializeDataStored) {
            frameAnalyser.faceList = mainViewModel.loadSerializedImageData(filesDir)
        } else {
            launchChooseDirectoryIntent()
        }
        setContent {
            FaceRecognitionTheme {
                val navComposable = rememberNavController()
                NavigationAppHost(navController = navComposable)
            }
        }
    }

    private fun launchChooseDirectoryIntent() {
        val intent = Intent( Intent.ACTION_OPEN_DOCUMENT_TREE )
        directoryAccessLauncher.launch( intent )
    }

    private val directoryAccessLauncher = registerForActivityResult( ActivityResultContracts.StartActivityForResult() ) {
        val dirUri = it.data?.data ?: return@registerForActivityResult
        val childrenUri =
            DocumentsContract.buildChildDocumentsUriUsingTree(
                dirUri,
                DocumentsContract.getTreeDocumentId( dirUri )
            )
        val tree = DocumentFile.fromTreeUri(this, childrenUri)
        val images = ArrayList<Pair<String,Bitmap>>()
        var errorFound = false
        if ( tree!!.listFiles().isNotEmpty()) {
            for ( doc in tree.listFiles() ) {
                if ( doc.isDirectory && !errorFound ) {
                    val name = doc.name!!
                    for ( imageDocFile in doc.listFiles() ) {
                        try {
                            images.add( Pair( name , getFixedBitmap( imageDocFile.uri ) ) )
                        }
                        catch ( e : Exception ) {
                            errorFound = true
                            break
                        }
                    }
                }
                else {
                    errorFound = true
                }
            }
        }
        else {
            errorFound = true
        }
        if ( !errorFound ) {
            fileReader.run( images , fileReaderCallback )
        }
    }

    private fun getFixedBitmap( imageFileUri : Uri) : Bitmap {
        var imageBitmap = BitmapUtils.getBitmapFromUri( contentResolver , imageFileUri )
        val exifInterface = ExifInterface( contentResolver.openInputStream( imageFileUri )!! )
        imageBitmap =
            when (exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION ,
                ExifInterface.ORIENTATION_UNDEFINED )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> BitmapUtils.rotateBitmap( imageBitmap , 90f )
                ExifInterface.ORIENTATION_ROTATE_180 -> BitmapUtils.rotateBitmap( imageBitmap , 180f )
                ExifInterface.ORIENTATION_ROTATE_270 -> BitmapUtils.rotateBitmap( imageBitmap , 270f )
                else -> imageBitmap
            }
        return imageBitmap
    }
}