package com.example.ladaroots.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceManager(private val context: Context) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    var onSpeechResult: ((String) -> Unit)? = null
    var onSpeechStateChanged: ((Boolean) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Malay (ms-MY), otherwise English
                val malay = Locale("ms", "MY")
                val result = tts?.setLanguage(malay)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.ENGLISH
                }
                isTtsReady = true
            }
        }
    }

    fun speak(text: String) {
        if (!isTtsReady || tts == null) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LadaRootsUtterance")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun startListening() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceManager)
                }
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ms-MY")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Sila sebut hasil lada atau arahan...")
            }
            try {
                speechRecognizer?.startListening(intent)
                onSpeechStateChanged?.invoke(true)
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "Ralat memulakan pengecaman suara.")
                onSpeechStateChanged?.invoke(false)
            }
        } else {
            // Speech recognizer not available on this emulator/device, provide friendly fallback
            onError?.invoke("Pengecaman suara sistem tidak tersedia di peranti ini.")
            onSpeechStateChanged?.invoke(false)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        onSpeechStateChanged?.invoke(false)
    }

    fun destroy() {
        stopSpeaking()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        onSpeechStateChanged?.invoke(false)
    }

    override fun onError(error: Int) {
        onSpeechStateChanged?.invoke(false)
        val msg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "Tiada suara dikesan."
            SpeechRecognizer.ERROR_AUDIO -> "Ralat mikrofon audio."
            SpeechRecognizer.ERROR_NETWORK -> "Ralat sambungan rangkaian suara."
            else -> "Pengecaman suara selesai."
        }
        onError?.invoke(msg)
    }

    override fun onResults(results: Bundle?) {
        onSpeechStateChanged?.invoke(false)
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onSpeechResult?.invoke(matches[0])
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
