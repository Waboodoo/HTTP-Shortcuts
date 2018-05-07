package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

abstract class BaseAction(val id: String, val actionType: BaseActionType, data: Map<String, String>) {

    protected val internalData = data.toMutableMap()

    fun toDTO() = ActionDTO(
            id = id,
            type = actionType.type,
            data = internalData
    )

    abstract fun getDescription(context: Context): CharSequence

    open fun perform(context: Context, shortcutId: Long, variableValues: Map<String, String>): Promise<Unit, Throwable, Unit> =
            try {
                performBlocking(context, shortcutId, variableValues)
                PromiseUtils.resolve(Unit)
            } catch (e: Throwable) {
                PromiseUtils.reject(e)
            }

    protected open fun performBlocking(context: Context, shortcutId: Long, variableValues: Map<String, String>) {

    }

    open fun edit(context: Context, showDelete: Boolean = false): Promise<Unit, Boolean, Unit> {
        val editorView = createEditorView(context) ?: return PromiseUtils.resolve(Unit)
        val deferred = DeferredObject<Unit, Boolean, Unit>()
        MaterialDialog.Builder(context)
                .title(actionType.title)
                .customView(editorView, true)
                .dismissListener {
                    if (deferred.isPending) {
                        deferred.reject(false)
                    }
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
                .mapIf(showDelete) {
                    it.neutralText(R.string.dialog_remove)
                            .onNeutral { dialog, _ ->
                                deferred.reject(true)
                                dialog.dismiss()
                            }
                }
                .showIfPossible()
                .let { dialogShown ->
                    if (!dialogShown) {
                        deferred.reject(false)
                    }
                }

        return deferred.promise()
                .always { _, _, _ -> editorView.destroy() }
    }

    abstract fun createEditorView(context: Context): BaseActionEditorView?


}