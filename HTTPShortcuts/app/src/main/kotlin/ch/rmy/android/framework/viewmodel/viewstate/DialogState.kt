package ch.rmy.android.framework.viewmodel.viewstate

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder

interface DialogState {
    fun createDialog(context: Context, viewModel: WithDialog): Dialog

    val id: String?

    fun saveInstanceState(dialog: Dialog): Bundle =
        dialog.onSaveInstanceState()

    fun restoreInstanceState(dialog: Dialog, saveInstanceState: Bundle) {
        dialog.onRestoreInstanceState(saveInstanceState)
    }

    companion object {
        fun create(id: String? = null, transform: DialogBuilder.(WithDialog) -> Dialog): DialogState =
            object : DialogState {
                override val id: String?
                    get() = id

                override fun createDialog(context: Context, viewModel: WithDialog) =
                    DialogBuilder(context)
                        .dismissListener {
                            viewModel.onDialogDismissed(id)
                        }
                        .transform(viewModel)
            }
    }
}
