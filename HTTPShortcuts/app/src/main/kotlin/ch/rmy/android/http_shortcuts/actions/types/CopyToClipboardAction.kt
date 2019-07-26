package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import com.android.volley.VolleyError
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class CopyToClipboardAction(
    actionType: CopyToClipboardActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var text: String
        get() = internalData[KEY_TEXT] ?: ""
        set(value) {
            internalData[KEY_TEXT] = value
        }

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable =
        Completable.fromAction {
            val text = text
            if (text.isNotEmpty()) {
                ClipboardUtil.copyToClipboard(context, text)
            }
        }
            .subscribeOn(AndroidSchedulers.mainThread())

    companion object {

        const val KEY_TEXT = "text"

    }

}