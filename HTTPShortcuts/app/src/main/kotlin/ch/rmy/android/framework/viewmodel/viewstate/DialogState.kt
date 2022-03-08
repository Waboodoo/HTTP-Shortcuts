package ch.rmy.android.framework.viewmodel.viewstate

import android.app.Dialog
import android.content.Context
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder

interface DialogState {
    fun createDialog(context: Context, viewModel: WithDialog): Dialog

    companion object {
        fun create(transform: DialogBuilder.(WithDialog) -> DialogBuilder): DialogState =
            object : DialogState {
                override fun createDialog(context: Context, viewModel: WithDialog) =
                    DialogBuilder(context)
                        .transform(viewModel)
                        .dismissListener {
                            viewModel.onDialogDismissed()
                        }
                        .build()
            }
    }
}
