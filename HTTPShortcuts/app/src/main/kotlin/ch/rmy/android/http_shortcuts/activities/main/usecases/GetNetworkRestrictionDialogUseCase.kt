package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.widget.CheckBox
import android.widget.TextView
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.Settings

class GetNetworkRestrictionDialogUseCase(
    private val settings: Settings,
) {

    operator fun invoke(): DialogState =
        DialogState.create { viewModel ->
            var doNotShowAgain = false

            this
                .positive(R.string.dialog_ok)
                .view(R.layout.dismissable_dialog)
                .dismissListener {
                    settings.isNetworkRestrictionWarningPermanentlyHidden = doNotShowAgain
                    viewModel.onDialogDismissed(null)
                }
                .build()
                .also {
                    val messageView = it.findViewById(R.id.dialog_message) as TextView
                    messageView.setText(R.string.warning_data_saver_battery_saver_enabled)
                    val checkBox = it.findViewById(R.id.checkbox_do_not_show_again) as CheckBox
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        doNotShowAgain = isChecked
                    }
                }
        }
}
