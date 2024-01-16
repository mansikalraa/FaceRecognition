package com.lattice.facerecognition.di

import android.content.Context
import com.lattice.facerecognition.FaceNetModel
import com.lattice.facerecognition.FileReader
import com.lattice.facerecognition.FrameAnalyser
import com.lattice.facerecognition.data.Models
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TensorFlowModule {

    @Provides
    @Singleton
    internal fun provideFaceNetModel(@ApplicationContext context: Context): FaceNetModel {
        return FaceNetModel( context, Models.FACENET, true, true )
    }

    @Provides
    @Singleton
    internal fun provideFrameAnalyzer(@ApplicationContext context: Context, faceNetModel: FaceNetModel): FrameAnalyser {
        return FrameAnalyser( context, faceNetModel )
    }

    @Provides
    @Singleton
    internal fun provideFileReader(faceNetModel: FaceNetModel): FileReader {
        return FileReader( faceNetModel )
    }
}