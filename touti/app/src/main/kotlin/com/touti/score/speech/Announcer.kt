package com.touti.score.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.touti.score.model.Lang
import com.touti.score.model.Line
import java.util.Locale

/**
 * Reads lines out with the phone's text-to-speech engine, one language after the other.
 *
 * Lines are chained through [UtteranceProgressListener.onDone] rather than queued up front,
 * because engines apply [TextToSpeech.setLanguage] at synthesis time: queuing a French then an
 * Arabic line would read both in Arabic.
 */
class Announcer(context: Context, private val onReady: (arabicAvailable: Boolean) -> Unit) {
    private val pending = ArrayDeque<Line>()
    private var ready = false
    private val tts = TextToSpeech(context.applicationContext) { status -> onInit(status) }

    private fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = speakNext()

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = speakNext()
        })
        ready = true
        onReady(isAvailable(ARABIC))
    }

    fun say(lines: List<Line>) {
        if (!ready) return
        synchronized(pending) {
            pending.clear()
            pending.addAll(lines)
        }
        // A new round cuts off the previous announcement instead of piling up behind it.
        tts.stop()
        speakNext()
    }

    fun shutdown() {
        tts.shutdown()
    }

    private fun speakNext() {
        while (true) {
            val line = synchronized(pending) { pending.removeFirstOrNull() } ?: return
            val locale = if (line.lang == Lang.FR) Locale.FRENCH else ARABIC
            // No voice for this language on the phone: skip the line rather than mispronounce it.
            if (!isAvailable(locale)) continue
            tts.setLanguage(locale)
            tts.speak(line.text, TextToSpeech.QUEUE_FLUSH, null, line.hashCode().toString())
            return
        }
    }

    private fun isAvailable(locale: Locale): Boolean =
        tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE

    private companion object {
        val ARABIC: Locale = Locale.forLanguageTag("ar")
    }
}
