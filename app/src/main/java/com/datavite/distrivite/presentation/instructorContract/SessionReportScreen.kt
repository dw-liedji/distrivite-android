package com.datavite.distrivite.presentation.instructorContract

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.datavite.distrivite.BuildConfig
import com.datavite.distrivite.app.BottomNavigationBar
import com.datavite.distrivite.presentation.components.PullToRefreshBox
import com.datavite.distrivite.presentation.components.TiqtaqCloudTopBar
import com.ramcosta.composedestinations.generated.destinations.InstructorContractScreenDestination
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberSaveableWebViewState
import com.kevinnzou.web.rememberWebViewNavigator
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Destination<RootGraph>
@Composable
fun SessionReportScreen(
    navigator: DestinationsNavigator,
    viewModel: InstructorContractViewModel
) {

    val authOrgUser by viewModel.authOrgUser.collectAsState()
    val sessionReportUrl by viewModel.sessionReportUrl.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val route = InstructorContractScreenDestination.route

    // Context for accessing system services like DownloadManager
    val context = LocalContext.current

    // Remember the WebView state and navigator
    val webViewState = rememberSaveableWebViewState()
    val webNavigator = rememberWebViewNavigator()

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TiqtaqCloudTopBar(
                scrollBehavior = scrollBehavior,
                destinationsNavigator = navigator,
                onSearchQueryChanged = { },
                onSearchClosed = { },
                onSync = { },
                onBackPressed = {
                    navigator.navigate(InstructorContractScreenDestination())
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(route = route, destinationsNavigator = navigator)
        }
    ) { paddings ->

        LaunchedEffect(webNavigator) {
            val bundle = webViewState.viewState
            if (bundle == null) {
                webNavigator.loadUrl("${BuildConfig.BASE_URL}${sessionReportUrl}")
            }
        }


        // Handle system back button
        BackHandler {
            navigator.navigate(InstructorContractScreenDestination())
        }

        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = {
                // Trigger reload when pulling to refresh
                Log.i("tiqtaqrefresh", "Trigger reload when pulling to refresh")
                isRefreshing = false
                webNavigator.reload()
            },

            modifier = Modifier.padding(paddings)
        ) {
            // WebView content inside PullToRefreshBox
            WebView(
                state = webViewState,
                navigator = webNavigator,
                modifier = Modifier.fillMaxSize(),
                onCreated = { webView ->
                    webView.settings.javaScriptEnabled = true

                    // Set up a WebViewClient to intercept PDF responses
                    webView.webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                            // Intercept the response headers for PDF content
                            val response = super.shouldInterceptRequest(view, request)
                            response?.let {
                                val contentType = response.responseHeaders?.get("Content-Type")
                                if (contentType == "application/pdf") {
                                    downloadPdf(context, request.url.toString())
                                    return null // Stop the WebView from loading the PDF, let the download manager handle it.
                                }
                            }
                            return response
                        }
                    }

                    webView.webChromeClient = WebChromeClient()

                    // Handle any regular downloads using the download listener
                    webView.setDownloadListener { url, _, _, mimeType, _ ->
                        if (mimeType == "application/pdf") {
                            downloadPdf(context, url)
                        }
                    }
                }
            )

            // Stop refreshing once the WebView finishes loading
            LaunchedEffect(webViewState.loadingState) {
                if (webViewState.loadingState !is LoadingState.Loading) {
                    isRefreshing = false
                }
            }
        }
    }
}

// Helper function to download PDF using DownloadManager
private fun downloadPdf(context: Context, url: String) {
    try {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Downloading PDF")

        // Ensure correct MIME type
        request.setMimeType("application/pdf")
        request.setDescription("The file is being downloaded.")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Extract file name from URL if not provided by server
        val fileName = uri.lastPathSegment ?: "downloaded_file.pdf"

        // Use appropriate directory based on Android version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
        } else {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Log.d("DownloadPDF", "Download started with ID: $downloadId for URL: $url")

    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("DownloadPDF", "Error downloading PDF: ${e.localizedMessage}")
        // Handle download error (e.g., show a Toast or SnackBar)
    }
}
