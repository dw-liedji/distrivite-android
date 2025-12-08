package com.datavite.distrivite.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SpeechRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognizerIntent: Intent? = null
    private var onResultsListener: ((String) -> Unit)? = null
    private var isListening = false

    fun initialize() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                // Only restart if the user is still listening
                if (isListening) {
                    restartListening()
                }
            }

            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                    if (it.isNotEmpty()) {
                        onResultsListener?.invoke(it[0])
                    }
                }
                // Only restart if the user is still listening
                if (isListening) {
                    restartListening()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening() {
        if (!isListening) {
            isListening = true
            speechRecognizer?.startListening(speechRecognizerIntent)
        }
    }

    fun stopListening() {
        if (isListening) {
            isListening = false
            speechRecognizer?.stopListening()
        }
    }

    fun destroy() {
        isListening = false
        speechRecognizer?.destroy()
    }

    private fun restartListening() {
        // Restart only if still listening
        if (isListening) {
            stopListening()
            startListening()
        }
    }

    fun setOnResultsListener(listener: (String) -> Unit) {
        onResultsListener = listener
    }
}
