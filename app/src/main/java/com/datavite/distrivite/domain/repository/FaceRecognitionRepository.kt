package com.datavite.distrivite.domain.repository

import FaceRecognitionAnalyzer
import androidx.lifecycle.LiveData
import com.datavite.distrivite.domain.model.DomainFaceRecognition

interface FaceRecognitionRepository {
    fun getFaceAnalyzer(): FaceRecognitionAnalyzer
    fun getFaceResults(): LiveData<List<DomainFaceRecognition>>
}