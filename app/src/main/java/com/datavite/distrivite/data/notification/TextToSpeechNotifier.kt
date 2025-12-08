package com.datavite.distrivite.data.notification

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.datavite.distrivite.R
import com.datavite.distrivite.domain.notification.NotificationEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject

class TextToSpeechNotifier @Inject constructor(
    @param: ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var initialized = false
    private var onInitDone: (() -> Unit)? = null

    fun setOnInitDone(callback: () -> Unit) {
        onInitDone = callback
        if (initialized) callback()
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.FRENCH
            tts.setSpeechRate(1.0f)
            tts.setPitch(1.0f)

            // Register earcons
            tts.addEarcon("success", context.packageName, R.raw.successed)
            tts.addEarcon("failure", context.packageName, R.raw.failure)
            tts.addEarcon("error", context.packageName, R.raw.error)

            initialized = true
            onInitDone?.invoke()
        } else {
            Log.e("TTSNotifier", "Initialization failed.")
        }
    }

    fun speak(event: NotificationEvent) {
        if (!initialized) {
            Log.w("TTSNotifier", "TTS not initialized yet.")
            return
        }

        //if (tts.isSpeaking) tts.stop()

        when (event) {
            is NotificationEvent.Success -> {
                tts.setSpeechRate(1.1f)
                tts.setPitch(1.2f)
                tts.playEarcon(
                    "success",
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "earcon-success"
                )
            }

            is NotificationEvent.Failure -> {
                tts.setSpeechRate(0.95f)
                tts.setPitch(0.9f)
                tts.playEarcon(
                    "failure",
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "earcon-failure"
                )
            }

            is NotificationEvent.Error -> {
                tts.setSpeechRate(0.85f)
                tts.setPitch(0.8f)
                tts.playEarcon(
                    "error",
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "earcon-error"
                )
            }
        }

        val result = tts.speak(event.message, TextToSpeech.QUEUE_ADD, null, "TTS_EVENT")
        if (result == TextToSpeech.ERROR) {
            Log.e("TTSNotifier", "Failed to speak: ${event.message}")
        }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
