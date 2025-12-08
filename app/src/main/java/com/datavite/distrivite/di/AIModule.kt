package com.datavite.distrivite.di

import android.content.Context
import com.datavite.distrivite.presentation.ai.InternalStorageFaceImageRepository
import com.datavite.distrivite.presentation.ai.FaceAntiSpoofingPlainDetector
import com.datavite.distrivite.presentation.ai.FaceDetectorStreamAnalyser
import com.datavite.distrivite.presentation.ai.FaceEmbeddingProcessor
import com.datavite.distrivite.presentation.ai.FaceImageCapturedCallback
import com.datavite.distrivite.presentation.ai.FaceRecogniser
import com.datavite.distrivite.presentation.ai.FaceRecognitionSecurePipeLine
import com.datavite.distrivite.presentation.ai.FaceSpoofingDetector
import com.datavite.distrivite.presentation.ai.FaceVideoStreamAnalyser
import com.datavite.distrivite.presentation.ai.MlkitFaceDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideInternalStorageFaceImageRepository(@ApplicationContext context: Context): InternalStorageFaceImageRepository {
        return InternalStorageFaceImageRepository(context = context)
    }

    @Provides
    @Singleton
    fun provideMlkitFaceDetector(): MlkitFaceDetector {
        return MlkitFaceDetector()
    }

    @Provides
    @Singleton
    fun provideFaceEmbeddingProcessor(@ApplicationContext context: Context): FaceEmbeddingProcessor {
        return FaceEmbeddingProcessor(context = context, useGpu = false, useXNNPack = false)
    }

    @Provides
    @Singleton
    fun provideFaceSpoofingDetector(@ApplicationContext context: Context): FaceSpoofingDetector {
        return FaceSpoofingDetector(context)
    }

    @Provides
    @Singleton
    fun provideFaceAntiSpoofingPlainDetector(@ApplicationContext context: Context): FaceAntiSpoofingPlainDetector {
        return FaceAntiSpoofingPlainDetector(context)
    }

    @Provides
    @Singleton
    fun provideFaceRecognitionPipeLine(faceAntiSpoofingPlainDetector: FaceAntiSpoofingPlainDetector, faceRecogniser: FaceRecogniser): FaceRecognitionSecurePipeLine {
        return FaceRecognitionSecurePipeLine(faceAntiSpoofingPlainDetector, faceRecogniser)
    }

    @Provides
    @Singleton
    fun provideFaceRecogniser(faceEmbeddingProcessor: FaceEmbeddingProcessor): FaceRecogniser {
        return FaceRecogniser(faceEmbeddingProcessor)
    }

    @Provides
    //@Singleton
    fun provideFaceVideoStreamAnalyser(): FaceVideoStreamAnalyser {
        return FaceVideoStreamAnalyser()
    }

    @Provides
    //@Singleton
    fun provideFaceDetectorStreamAnalyser(): FaceDetectorStreamAnalyser {
        return FaceDetectorStreamAnalyser()
    }

    @Provides
    //@Singleton
    fun provideFaceImageCapturedCallback(): FaceImageCapturedCallback {
        return FaceImageCapturedCallback()
    }
}