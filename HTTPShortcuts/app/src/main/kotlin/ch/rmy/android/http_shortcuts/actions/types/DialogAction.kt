package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.afollestad.materialdialogs.MaterialDialog
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

    override fun getDescription(context: Context): CharSequence =
        context.getString(R.string.action_type_dialog_description, message)

    override fun perform(context: Context, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(message, variableValues)
        return if (finalMessage.isNotEmpty()) {
            Completable.create { emitter ->
                (context as ExecuteActivity).runOnUiThread {
                    MaterialDialog.Builder(context)
                        //.title(shortcutName) // TODO
                        .content(finalMessage)
                        .positiveText(R.string.dialog_ok)
                        .dismissListener { emitter.onComplete() }
                        .show()
                }
            }
                .subscribeOn(AndroidSchedulers.mainThread())
        } else {
            Completable.complete()
        }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
        DialogActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_TEXT = "text"

    }

}