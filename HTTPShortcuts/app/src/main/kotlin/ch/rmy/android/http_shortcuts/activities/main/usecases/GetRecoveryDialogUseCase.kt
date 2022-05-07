package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.models.RecoveryInfo
import javax.inject.Inject

class GetRecoveryDialogUseCase
@Inject
constructor() {

    operator fun invoke(recoveryInfo: RecoveryInfo, onRecover: (RecoveryInfo) -> Unit, onDiscard: () -> Unit): DialogState =
        DialogState.create(id = "recovery") {
            title(R.string.title_unsaved_changes_detected)
                .message(
                    if (recoveryInfo.shortcutName.isNotEmpty()) {
                        StringResLocalizable(R.string.message_unsaved_changes_detected, recoveryInfo.shortcutName)
                    } else {
                        StringResLocalizable(R.string.message_unsaved_changes_detected_no_name)
                    }
                )
                .canceledOnTouchOutside(false)
                .positive(R.string.button_recover) {
                    onRecover(recoveryInfo)
                }
                .negative(R.string.dialog_discard) {
                    onDiscard()
                }
                .build()
        }
}
