package com.datavite.distrivite.data.repository

import FaceRecognitionAnalyzer
import androidx.lifecycle.LiveData
import com.datavite.distrivite.domain.model.DomainFaceRecognition
import com.datavite.distrivite.domain.repository.FaceRecognitionRepository

class TfLiteFaceRecognitionRepositoryImpl(private val analyzer: FaceRecognitionAnalyzer):
    FaceRecognitionRepository {
    override fun getFaceAnalyzer(): FaceRecognitionAnalyzer {
        return analyzer
    }

    override fun getFaceResults(): LiveData<List<DomainFaceRecognition>> {
        return analyzer.getDatasource().FaceRecognitions()
    }
}