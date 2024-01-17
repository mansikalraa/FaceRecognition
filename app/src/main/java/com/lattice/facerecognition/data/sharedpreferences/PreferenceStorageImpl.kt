package com.lattice.facerecognition.data.sharedpreferences

import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceStorageImpl(private val sharedPreferences: SharedPreferences) : PreferenceStorage {

    override var serializeDataStored: Boolean
        get() = sharedPreferences.getBoolean(SERIALIZE_DATA, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(SERIALIZE_DATA, value)
                commit()
            }
        }

    companion object {
        const val PREFS_NAME = "face_pref"
        const val SERIALIZE_DATA = "serialized_data"
    }
}