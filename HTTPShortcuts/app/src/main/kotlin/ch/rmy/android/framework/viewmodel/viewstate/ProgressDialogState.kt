package ch.rmy.android.framework.viewmodel.viewstate

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.WithDialog

class ProgressDialogState(
    private val message: Localizable,
    private val onCanceled: () -> Unit,
) : DialogState {
    override val id = DIALOG_ID
    override fun createDialog(context: Context, viewModel: WithDialog?): Dialog =
        ProgressDialog(context).apply {
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
