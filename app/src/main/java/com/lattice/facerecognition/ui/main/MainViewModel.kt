package com.lattice.facerecognition.ui.main

import androidx.lifecycle.ViewModel
import com.lattice.facerecognition.data.sharedpreferences.PreferenceStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val preferenceStorage: PreferenceStorage): ViewModel() {

    internal fun saveSerializedImageData(filesDir: File, data : ArrayList<Pair<String,FloatArray>> ) {
        val serializedDataFile = File( filesDir , SERIALIZED_DATA_FILENAME )
        if (serializedDataFile.exists()) serializedDataFile.delete()
        ObjectOutputStream( FileOutputStream( serializedDataFile )  ).apply {
            writeObject( data )
            flush()
            close()
        }
        preferenceStorage.serializeDataStored = true
    }


    internal fun loadSerializedImageData(filesDir: File) : ArrayList<Pair<String,FloatArray>> {
        val serializedDataFile = File( filesDir , SERIALIZED_DATA_FILENAME )
        val objectInputStream = ObjectInputStream( FileInputStream( serializedDataFile ) )
        val data = objectInputStream.readObject() as ArrayList<Pair<String,FloatArray>>
        objectInputStream.close()
        return data
    }

    companion object {
        private const val SERIALIZED_DATA_FILENAME = "image_data"
    }

}