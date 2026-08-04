package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class PlaybackState {
    IDLE, PLAYING, PAUSED
}

data class AudioQueueItem(
    val title: String,
    val bullets: List<String>,
    val publisher: String
)

class AudioDigestManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _currentSpeechSpeed = MutableStateFlow(1.0f)
    val currentSpeechSpeed: StateFlow<Float> = _currentSpeechSpeed.asStateFlow()

    private val _currentProgressIndex = MutableStateFlow(0)
    val currentProgressIndex: StateFlow<Int> = _currentProgressIndex.asStateFlow()

    private var currentQueue: List<AudioQueueItem> = emptyList()
    private var currentItemIndex = 0

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isInitialized = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _playbackState.value = PlaybackState.PLAYING
                }

                override fun onDone(utteranceId: String?) {
                    if (currentItemIndex + 1 < currentQueue.size) {
                        currentItemIndex++
                        playCurrentQueueItem()
                    } else {
                        _playbackState.value = PlaybackState.IDLE
                        _currentTitle.value = ""
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _playbackState.value = PlaybackState.IDLE
                }
            })
        }
    }

    fun playArticle(title: String, publisher: String, bullets: List<String>) {
        val queueItem = AudioQueueItem(title, bullets, publisher)
        currentQueue = listOf(queueItem)
        currentItemIndex = 0
        playCurrentQueueItem()
    }

    fun playDigestQueue(queue: List<AudioQueueItem>) {
        if (queue.isEmpty()) return
        currentQueue = queue
        currentItemIndex = 0
        playCurrentQueueItem()
    }

    private fun playCurrentQueueItem() {
        if (!isInitialized || currentQueue.isEmpty() || currentItemIndex >= currentQueue.size) return

        val item = currentQueue[currentItemIndex]
        _currentTitle.value = item.title

        val speechText = StringBuilder()
            .append("Bản tin Claritas từ ").append(item.publisher).append(". ")
            .append("Tiêu đề: ").append(item.title).append(". ")
            .append("Tóm tắt 3 điểm chính: ")

        item.bullets.forEachIndexed { idx, bullet ->
            speechText.append("Điểm ").append(idx + 1).append(": ").append(bullet).append(". ")
        }

        tts?.setSpeechRate(_currentSpeechSpeed.value)
        tts?.speak(speechText.toString(), TextToSpeech.QUEUE_FLUSH, null, "CLARITAS_UTTERANCE_$currentItemIndex")
        _playbackState.value = PlaybackState.PLAYING
    }

    fun pause() {
        if (_playbackState.value == PlaybackState.PLAYING) {
            tts?.stop()
            _playbackState.value = PlaybackState.PAUSED
        }
    }

    fun resume() {
        if (_playbackState.value == PlaybackState.PAUSED) {
            playCurrentQueueItem()
        }
    }

    fun stop() {
        tts?.stop()
        _playbackState.value = PlaybackState.IDLE
        _currentTitle.value = ""
    }

    fun setSpeed(speed: Float) {
        _currentSpeechSpeed.value = speed
        tts?.setSpeechRate(speed)
        if (_playbackState.value == PlaybackState.PLAYING) {
            playCurrentQueueItem()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
