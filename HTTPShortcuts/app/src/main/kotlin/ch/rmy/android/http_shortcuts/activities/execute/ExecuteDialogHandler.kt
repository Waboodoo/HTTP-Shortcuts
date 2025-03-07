package ch.rmy.android.http_shortcuts.activities.execute

import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExecuteDialogHandler
@Inject
constructor() : DialogHandle {
    private var dialogResult: CompletableDeferred<Any>? = null
    private val _dialogState = MutableStateFlow<ExecuteDialogState<*>?>(null)
    val dialogState = _dialogState.asStateFlow()

    override suspend fun <T : Any> showDialog(dialogState: ExecuteDialogState<T>): T {
        dialogResult?.cancel()
        val dialogResult = CompletableDeferred<Any>()
        this.dialogResult = dialogResult
        _dialogState.value = dialogState
        return dialogResult.await() as T
    }

    fun onDialogDismissed() {
        _dialogState.value = null
        val dialogResult = dialogResult
        this.dialogResult = null
        dialogResult?.cancel(DialogCancellationException())
    }

    fun onDialogResult(result: Any) {
        _dialogState.value = null
        val dialogResult = dialogResult
        this.dialogResult = null
        dialogResult?.complete(result)
    }
}
