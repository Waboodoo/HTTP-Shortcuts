package ch.rmy.android.http_shortcuts.activities.execute

interface DialogHandle {

    suspend fun showDialog(dialogState: ExecuteDialogState): Any?
}
