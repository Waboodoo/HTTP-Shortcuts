package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.android.volley.VolleyError
import io.reactivex.Completable

abstract class BaseAction(
    val actionType: BaseActionType,
    data: Map<String, String>
) {

    protected val internalData = data.toMutableMap()

    fun toDTO() = ActionDTO(
        type = actionType.type,
        data = internalData
    )

    // TODO: Should this be removed?
    abstract fun getDescription(context: Context): CharSequence

    open fun perform(context: Context, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        performBlocking(context, shortcutId, variableValues, response, volleyError, recursionDepth)
        return Completable.complete()
    }

    protected open fun performBlocking(context: Context, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int) {

    }

    open fun edit(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider): Completable {
        val editorView = createEditorView(context, variablePlaceholderProvider)
            ?: return Completable.complete()

        return Completable.create { emitter ->
            MaterialDialog.Builder(context)
                .title(actionType.title)
                .customView(editorView, true)
                .dismissListener {
                    emitter.cancel()
                }
                .positiveText(R.string.dialog_ok)
                .onPositive { dialog, _ ->
                    val success = editorView.compile()
                    if (success) {
                        emitter.onComplete()
                        dialog.dismiss()
                    }
                }
                .autoDismiss(false)
                .negativeText(R.string.dialog_cancel)
                .onNegative { dialog, _ -> dialog.dismiss() }
                .showIfPossible()
                ?: run {
                    emitter.cancel()
                }
        }
            .doOnTerminate {
                editorView.destroyer
            }
    }

    open fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider): BaseActionEditorView? = null

}