package com.lattice.facerecognition.di

import com.lattice.facerecognition.data.sharedpreferences.PreferenceStorage
import com.lattice.facerecognition.data.sharedpreferences.PreferenceStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PreferenceModule {

    @Binds
    @Singleton
    fun providePreferenceStorage(preferenceStorageImpl: PreferenceStorageImpl): PreferenceStorage
}