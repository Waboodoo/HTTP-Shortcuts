package ch.rmy.android.http_shortcuts.activities.execute

interface DialogHandle {
    suspend fun <T : Any> showDialog(dialogState: ExecuteDialogState<T>): T
}
