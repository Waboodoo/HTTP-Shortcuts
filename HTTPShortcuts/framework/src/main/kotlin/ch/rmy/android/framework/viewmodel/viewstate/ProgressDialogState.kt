package ch.rmy.android.framework.viewmodel.viewstate

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.WithDialog

@Deprecated("Remove once fully migrated to Compose")
class ProgressDialogState(
    private val message: Localizable,
    private val onCanceled: () -> Unit,
) : DialogState {
    override val id = DIALOG_ID
    override fun createDialog(activity: Activity, viewModel: WithDialog?): Dialog =
        ProgressDialog(activity).apply {
            setMessage(message.localize(context))
            setCanceledOnTouchOutside(false)
            setOnCancelListener {
                onCanceled()
            }
        }

    companion object {
        const val DIALOG_ID = "progress"
    }
}
