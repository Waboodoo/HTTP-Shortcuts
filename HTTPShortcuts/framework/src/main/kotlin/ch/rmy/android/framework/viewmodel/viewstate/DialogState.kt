package ch.rmy.android.framework.viewmodel.viewstate

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import ch.rmy.android.framework.viewmodel.WithDialog

@Deprecated("Remove once fully migrated to Compose")
interface DialogState {
    fun createDialog(activity: Activity, viewModel: WithDialog? = null): Dialog

    val id: String?
        get() = null

    fun saveInstanceState(dialog: Dialog): Bundle =
        dialog.onSaveInstanceState()

    fun restoreInstanceState(dialog: Dialog, saveInstanceState: Bundle) {
        dialog.onRestoreInstanceState(saveInstanceState)
    }
}
