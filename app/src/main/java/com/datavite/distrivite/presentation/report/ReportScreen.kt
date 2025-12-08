package com.datavite.distrivite.presentation.report

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {

    val pdfUrl: String = "http://192.168.1.105:8000/en/receipt/"
    val downloadStatus = viewModel.downloadStatus.collectAsState()
    val pdfFile by viewModel.pdfFile.observeAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val status = downloadStatus.value) {
            is ReportViewModel.DownloadStatus.Idle -> {
                Button(onClick = { viewModel.downloadPdf(pdfUrl) }) {
                    Text("Download PDF")
                }
            }
            is ReportViewModel.DownloadStatus.Downloading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Downloading...", textAlign = TextAlign.Center)
                }
            }
            is ReportViewModel.DownloadStatus.Complete -> {
                Text("Download Complete!\nSaved to: ${status.filePath}", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    pdfFile?.let { viewModel.openPdf(it) }
                }) {
                    Text("View PDF")
                }
            }
            is ReportViewModel.DownloadStatus.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${status.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.downloadPdf(pdfUrl) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
