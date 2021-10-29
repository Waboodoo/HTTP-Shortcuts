package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.extensions.truncate
import ch.rmy.android.http_shortcuts.extensions.tryOrIgnore
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.Locale

class TextToSpeechAction(private val message: String, private val language: String) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, executionContext.variableManager.getVariableValuesByIds())
            .truncate(MAX_TEXT_LENGTH)
        return if (finalMessage.isNotEmpty()) {
            var tts: TextToSpeech? = null
            Completable
                .create { emitter ->
                    val id = newUUID()
                    val handler = Handler(Looper.getMainLooper())

                    tts = TextToSpeech(executionContext.context) { code ->
                        if (code != TextToSpeech.SUCCESS) {
                            emitter.onError(ActionException { it.getString(R.string.error_tts_failed) })
                            return@TextToSpeech
                        }

                        handler.post {
                            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onDone(utteranceId: String?) {
                                    emitter.onComplete()
                                }

                                override fun onError(utteranceId: String?) {
                                    if (utteranceId == id) {
                                        emitter.onError(ActionException { it.getString(R.string.error_tts_failed) })
                                    }
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
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnEvent {
                    tts?.stop()
                    tts?.shutdown()
                }
        } else {
            Completable.complete()
        }
    }

    companion object {

        private const val MAX_TEXT_LENGTH = 400

    }

}