package ch.rmy.android.framework.viewmodel

import ch.rmy.android.framework.viewmodel.viewstate.DialogState

interface WithDialog {
    fun onDialogDismissed(dialogState: DialogState) {
        if (this.dialogState == dialogState) {
            this.dialogState = null
        }
    }

    var dialogState: DialogState?
}
