package com.example.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.models.AnimeVoiceStyle
import com.example.data.models.AudioTrackItem
import com.example.data.models.AudioType
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

class AiVoiceService(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                tts?.language = Locale.US
            }
        }
    }

    fun isReady(): Boolean = isTtsReady

    fun validateProviderSetup(useCloudProvider: Boolean, cloudApiKey: String?): String? {
        if (useCloudProvider) {
            if (cloudApiKey.isNullOrBlank()) {
                return "External AI Voice provider key is missing. Please configure your API key in Settings, or use device Neural TTS."
            }
        }
        if (!isTtsReady && !useCloudProvider) {
            return "Device Text-to-Speech engine is initializing. Please wait a moment."
        }
        return null
    }

    fun previewVoice(
        text: String,
        voiceStyle: AnimeVoiceStyle,
        pitchMultiplier: Float = 1.0f,
        speedMultiplier: Float = 1.0f,
        languageCode: String = "en"
    ) {
        val engine = tts ?: return
        if (!isTtsReady) return

        engine.stop()
        val loc = if (languageCode == "hi") Locale.forLanguageTag("hi-IN") else Locale.US
        engine.language = loc
        engine.setPitch(voiceStyle.pitch * pitchMultiplier)
        engine.setSpeechRate(voiceStyle.rate * speedMultiplier)

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "preview_${System.currentTimeMillis()}")
    }

    fun stopPlayback() {
        tts?.stop()
    }

    suspend fun synthesizeToAudioTrack(
        text: String,
        voiceStyle: AnimeVoiceStyle,
        pitchMultiplier: Float = 1.0f,
        speedMultiplier: Float = 1.0f,
        languageCode: String = "en"
    ): Result<AudioTrackItem> = suspendCancellableCoroutine { continuation ->
        val engine = tts
        if (engine == null || !isTtsReady) {
            continuation.resume(Result.failure(IllegalStateException("TextToSpeech engine is not initialized on device. Please wait or check TTS in system settings.")))
            return@suspendCancellableCoroutine
        }

        try {
            val audioDir = File(context.cacheDir, "generated_voices").apply { mkdirs() }
            val audioFile = File(audioDir, "voice_${System.currentTimeMillis()}.wav")
            val utteranceId = "synth_${UUID.randomUUID()}"

            val loc = if (languageCode == "hi") Locale.forLanguageTag("hi-IN") else Locale.US
            engine.language = loc
            engine.setPitch(voiceStyle.pitch * pitchMultiplier)
            engine.setSpeechRate(voiceStyle.rate * speedMultiplier)

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    val wordCount = text.trim().split("\\s+".toRegex()).size
                    val estimatedDurationMs = ((wordCount / (2.5f * (voiceStyle.rate * speedMultiplier))) * 1000).toLong()
                        .coerceAtLeast(3000L)

                    val track = AudioTrackItem(
                        id = UUID.randomUUID().toString(),
                        uriString = audioFile.absolutePath,
                        title = "${voiceStyle.title} - Voiceover",
                        startTimelineMs = 0L,
                        durationMs = estimatedDurationMs,
                        volume = 1.0f,
                        isVoiceover = true,
                        speechVoice = voiceStyle.title,
                        audioType = AudioType.VOICEOVER,
                        fadeInMs = 0L,
                        fadeOutMs = 300L
                    )
                    if (continuation.isActive) {
                        continuation.resume(Result.success(track))
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(RuntimeException("Audio synthesis error occurred during recording.")))
                    }
                }
            })

            val result = engine.synthesizeToFile(text, params, audioFile, utteranceId)
            if (result != TextToSpeech.SUCCESS) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(RuntimeException("Failed to start voice synthesis with system engine.")))
                }
            }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
