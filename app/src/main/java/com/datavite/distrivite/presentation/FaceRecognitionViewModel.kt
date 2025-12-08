package com.datavite.distrivite.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datavite.distrivite.presentation.ai.InternalStorageFaceImageRepository
import com.datavite.distrivite.domain.model.DomainFaceRecognition
import com.datavite.distrivite.domain.repository.FaceRecognitionRepository
import kotlinx.coroutines.launch

class FaceRecognitionViewModel (private val fileRepository: InternalStorageFaceImageRepository, val repository: FaceRecognitionRepository) : ViewModel() {

    private val _faceRecognitionResults = MutableLiveData<List<DomainFaceRecognition>>()
    val faceRecognitionResults: LiveData<List<DomainFaceRecognition>> = _faceRecognitionResults

    private val _matchedResults = MutableLiveData<List<DomainFaceRecognition>>()
    val matchedResults: LiveData<List<DomainFaceRecognition>> = _matchedResults

    init {
        // Observe face recognition results and find matches
        viewModelScope.launch {
            repository.getFaceResults().observeForever { results ->
                _faceRecognitionResults.value = results
                Log.i("FaceRecognitiondistrivite", "finding matching")
                for (face in results) Log.i("FaceRecognitiondistrivite", "${face.face}")
            }
        }
        fileRepository
    }

    fun getFaceAnalyzer() = repository.getFaceAnalyzer()


}
