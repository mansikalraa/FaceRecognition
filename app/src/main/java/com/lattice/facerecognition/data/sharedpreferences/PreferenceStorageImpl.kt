package com.lattice.facerecognition.data.sharedpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class PreferenceStorageImpl @Inject constructor(private val sharedPreferences: SharedPreferences): PreferenceStorage {

    override var serializeDataStored: Boolean
        get() = sharedPreferences.getBoolean(SERIALIZE_DATA, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(SERIALIZE_DATA, value)
                commit()
            }
        }

    companion object {
        const val SERIALIZE_DATA = "serialized_data"
    }
}