package ch.rmy.android.framework.viewmodel.viewstate

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import ch.rmy.android.framework.viewmodel.WithDialog

interface DialogState {
    fun createDialog(context: Context, viewModel: WithDialog? = null): Dialog

    val id: String?
        get() = null

    fun saveInstanceState(dialog: Dialog): Bundle =
        dialog.onSaveInstanceState()

    fun restoreInstanceState(dialog: Dialog, saveInstanceState: Bundle) {
        dialog.onRestoreInstanceState(saveInstanceState)
    }
}
