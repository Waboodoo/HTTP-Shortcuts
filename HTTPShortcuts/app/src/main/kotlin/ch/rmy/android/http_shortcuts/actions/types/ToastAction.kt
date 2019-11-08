package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class ToastAction(
    actionType: ToastActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var message: String
        get() = internalData[KEY_TEXT] ?: ""
        set(value) {
            internalData[KEY_TEXT] = value
        }

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, variableManager.getVariableValuesByIds())
        return if (finalMessage.isNotEmpty()) {
            Completable.fromAction {
                context.showToast(finalMessage, long = true)
            }
                .subscribeOn(AndroidSchedulers.mainThread())
        } else {
            Completable.complete()
        }
    }

    companion object {

        const val KEY_TEXT = "text"

    }

}