package com.datavite.distrivite.presentation.components
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComponent(modifier: Modifier = Modifier, paddingValues: PaddingValues){
    val url = remember { mutableStateOf("https://www.distrivite.com/fr/users/account/") }
    val visibility = remember { mutableStateOf(false) }
    val progress = remember { mutableFloatStateOf(0.0F) }

    Box(
        modifier = modifier.padding(paddingValues),
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically

            ) {
                Row(
                    modifier = Modifier
                        .height(0.dp) // do not show these buttons
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .wrapContentHeight(Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        url.value = "https://in.linkedin.com/jobs/engineering-jobs-hyderabad?trk=homepage-basic_suggested-search&position=1&pageNum=0"
                    }) {
                        Text(text = "Linkedin")
                    }
                    Button(onClick = {
                        url.value = "https://developer.android.com/jetpack/compose"
                    }) {
                        Text(text = "Jetpack Compose")
                    }
                }

                if (visibility.value){
                    CircularProgressIndicator(
                        color = Color(0xFFE30022)
                    )
                    Text(
                        text = "${progress.floatValue.roundToInt()}%",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(2F)
            ) {
                AndroidView(factory = { context ->
                    WebView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true

                        webViewClient = object: WebViewClient(){
                            override fun onPageStarted(
                                view: WebView, url: String,
                                favicon: Bitmap?) {
                                visibility.value = true
                            }

                            override fun onPageFinished(
                                view: WebView, url: String) {
                                visibility.value = false
                            }
                        }

                        // Set web view chrome client
                        webChromeClient = object: WebChromeClient(){
                            override fun onProgressChanged(
                                view: WebView, newProgress: Int) {
                                progress.floatValue = newProgress.toFloat()
                            }
                        }

                        loadUrl(url.value)
                    }
                },update = {
                    it.loadUrl(url.value)
                })
            }
        }
    }
}