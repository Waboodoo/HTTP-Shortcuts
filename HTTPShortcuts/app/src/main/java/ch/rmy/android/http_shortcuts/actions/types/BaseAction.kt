package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import ch.rmy.android.http_shortcuts.utils.rejectSafely
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.android.volley.VolleyError
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

abstract class BaseAction(
        val id: String,
        val actionType: BaseActionType,
        data: Map<String, String>
) {

    protected val internalData = data.toMutableMap()

    fun toDTO() = ActionDTO(
            id = id,
            type = actionType.type,
            data = internalData
    )

    abstract fun getDescription(context: Context): CharSequence

    open fun perform(context: Context, shortcutId: Long, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Promise<Unit, Throwable, Unit> =
            try {
                performBlocking(context, shortcutId, variableValues, response, volleyError, recursionDepth)
                PromiseUtils.resolve(Unit)
            } catch (e: Throwable) {
                PromiseUtils.reject(e)
            }

    protected open fun performBlocking(context: Context, shortcutId: Long, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int) {

    }

    open fun edit(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider): Promise<Unit, Unit, Unit> {
        val editorView = createEditorView(context, variablePlaceholderProvider)
                ?: return PromiseUtils.resolve(Unit)
        val deferred = DeferredObject<Unit, Unit, Unit>()
        MaterialDialog.Builder(context)
                .title(actionType.title)
                .customView(editorView, true)
                .dismissListener {
                    deferred.rejectSafely(Unit)
                }
                .positiveText(R.string.dialog_ok)
                .onPositive { dialog, _ ->
                    val success = editorView.compile()
                    if (success) {
                        deferred.resolve(Unit)
                        dialog.dismiss()
                    }
                }
                .autoDismiss(false)
                .negativeText(R.string.dialog_cancel)
                .onNegative { dialog, _ -> dialog.dismiss() }
                .showIfPossible()
                .let { dialogShown ->
                    if (!dialogShown) {
                        deferred.rejectSafely(Unit)
                    }
                }

        return deferred.promise()
                .always { _, _, _ -> editorView.destroy() }
    }

    open fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider): BaseActionEditorView? = null

}