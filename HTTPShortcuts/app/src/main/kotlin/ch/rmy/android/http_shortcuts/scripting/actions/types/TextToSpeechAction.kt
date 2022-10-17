package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.extensions.tryOrIgnore
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TextToSpeechAction(private val message: String, private val language: String) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, executionContext.variableManager.getVariableValuesByIds())
            .truncate(MAX_TEXT_LENGTH)
        if (finalMessage.isEmpty()) {
            return
        }
        var tts: TextToSpeech? = null
        try {
            withContext(Dispatchers.Main) {
                suspendCoroutine<Unit> { continuation ->
                    val id = newUUID()
                    val handler = Handler(Looper.getMainLooper())

                    tts = TextToSpeech(executionContext.context) { code ->
                        if (code != TextToSpeech.SUCCESS) {
                            continuation.resumeWithException(ActionException { it.getString(R.string.error_tts_failed) })
                            return@TextToSpeech
                        }

                        handler.post {
                            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onDone(utteranceId: String?) {
                                    continuation.resume()
                                }

                                override fun onError(utteranceId: String?, errorCode: Int) {
                                    if (utteranceId == id) {
                                        continuation.resumeWithException(ActionException { it.getString(R.string.error_tts_failed) })
                                    }
                                }

                                @Deprecated("Deprecated in Java")
                                override fun onError(utteranceId: String?) {
                                }

                                override fun onStart(utteranceId: String?) {
                                }
                            })
                            if (language.isNotEmpty()) {
                                tryOrIgnore {
                                    tts!!.setLanguage(Locale.forLanguageTag(language))
                                }
                            }
                            tts!!.speak(finalMessage, TextToSpeech.QUEUE_FLUSH, null, id)
                        }
                    }
                }
            }
        } finally {
            tts?.stop()
            tts?.shutdown()
        }
    }

    companion object {

        private const val MAX_TEXT_LENGTH = 400
    }
}
