package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class DialogAction(
    actionType: DialogActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var message: String
        get() = internalData[KEY_TEXT] ?: ""
        set(value) {
            internalData[KEY_TEXT] = value
        }

    var title: String
        get() = internalData[KEY_TITLE] ?: ""
        set(value) {
            internalData[KEY_TITLE] = value
        }

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, variableManager.getVariableValuesByIds())
        return if (finalMessage.isNotEmpty()) {
            Completable.create { emitter ->
                (context as ExecuteActivity).runOnUiThread {
                    DialogBuilder(context)
                        .title(title)
                        .message(HTMLUtil.format(finalMessage))
                        .positive(R.string.dialog_ok)
                        .dismissListener { emitter.onComplete() }
                        .show()
                }
            }
                .subscribeOn(AndroidSchedulers.mainThread())
        } else {
            Completable.complete()
        }
    }

    companion object {

        const val KEY_TEXT = "text"
        const val KEY_TITLE = "title"

    }

}