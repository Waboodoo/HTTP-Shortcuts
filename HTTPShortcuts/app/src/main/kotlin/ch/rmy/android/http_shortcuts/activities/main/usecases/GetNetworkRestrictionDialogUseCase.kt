package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.widget.CheckBox
import android.widget.TextView
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.Settings
import com.afollestad.materialdialogs.callbacks.onShow

class GetNetworkRestrictionDialogUseCase(
    private val settings: Settings,
) {

    operator fun invoke(): DialogState =
        DialogState.create {
            positive(R.string.dialog_ok)
                .view(R.layout.dismissable_dialog)
                .build()
                .onShow { dialog ->
                    val messageView = dialog.findViewById(R.id.dialog_message) as TextView
                    messageView.setText(R.string.warning_data_saver_battery_saver_enabled)
                    val checkBox = dialog.findViewById(R.id.checkbox_do_not_show_again) as CheckBox
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        settings.isNetworkRestrictionWarningPermanentlyHidden = isChecked
                    }
                }
        }
}
