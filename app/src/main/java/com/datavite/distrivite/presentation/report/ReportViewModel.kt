package com.datavite.distrivite.presentation.report

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor( application: Application) : AndroidViewModel(application) {

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus

    private val _pdfFile = MutableLiveData<File>()
    val pdfFile: LiveData<File> = _pdfFile

    // Configure OkHttpClient with increased timeout durations
    private val client = OkHttpClient.Builder()
        .connectTimeout(180, TimeUnit.SECONDS)  // Increase connect timeout
        .writeTimeout(180, TimeUnit.SECONDS)    // Increase write timeout
        .readTimeout(180, TimeUnit.SECONDS)     // Increase read timeout
        .build()

    fun downloadPdf(pdfUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _downloadStatus.value = DownloadStatus.Downloading

                val request = Request.Builder()
                    .url(pdfUrl)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Failed to download file: $response")

                val contentType = response.header("Content-Type")
                if (contentType != "application/pdf") {
                    throw IOException("Unexpected content type: $contentType")
                }

                val fileName = "generated_report.pdf"
                val file = File(getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

                // Delete the existing file if it exists
                if (file.exists()) {
                    file.delete()
                }

                response.body?.let { body ->
                    val sink = file.sink()
                    sink.buffer().use {
                        it.writeAll(body.source())
                    }
                    _downloadStatus.value = DownloadStatus.Complete(file.absolutePath)
                    Log.d("PdfDownloadViewModel", "File downloaded: ${file.absolutePath}")

                    _pdfFile.postValue(file)
                    openPdf(file)
                }
            } catch (e: Exception) {
                _downloadStatus.value = DownloadStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

     fun openPdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        getApplication<Application>().startActivity(intent)
    }


    sealed class DownloadStatus {
        object Idle : DownloadStatus()
        object Downloading : DownloadStatus()
        data class Complete(val filePath: String) : DownloadStatus()
        data class Error(val message: String) : DownloadStatus()
    }
}
