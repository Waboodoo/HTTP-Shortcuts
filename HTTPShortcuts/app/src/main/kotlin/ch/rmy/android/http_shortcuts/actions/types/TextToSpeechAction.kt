package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.extensions.truncate
import ch.rmy.android.http_shortcuts.extensions.tryOrIgnore
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.Locale

class TextToSpeechAction(
    actionType: TextToSpeechActionType,
    data: Map<String, String>
) : BaseAction(actionType) {

    private val message: String = data[KEY_TEXT] ?: ""
    private val language: String = data[KEY_LANGUAGE] ?: ""

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, responseError: ErrorResponse?, recursionDepth: Int): Completable {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, variableManager.getVariableValuesByIds())
            .truncate(MAX_TEXT_LENGTH)
        return if (finalMessage.isNotEmpty()) {
            var tts: TextToSpeech? = null
            Completable
                .create { emitter ->
                    val id = newUUID()
                    val handler = Handler()

                    tts = TextToSpeech(context) { code ->
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

        const val KEY_TEXT = "text"
        const val KEY_LANGUAGE = "language"

        private const val MAX_TEXT_LENGTH = 400

    }

}