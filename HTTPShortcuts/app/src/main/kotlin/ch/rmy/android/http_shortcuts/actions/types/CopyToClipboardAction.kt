package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class CopyToClipboardAction(
    actionType: CopyToClipboardActionType,
    data: Map<String, String>
) : BaseAction(actionType) {

    private val text: String = data[KEY_TEXT] ?: ""

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, responseError: ErrorResponse?, recursionDepth: Int): Completable =
        Completable
            .fromAction {
                text
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        ClipboardUtil.copyToClipboard(context, it)
                    }
            }
            .subscribeOn(AndroidSchedulers.mainThread())

    companion object {

        const val KEY_TEXT = "text"

    }

}