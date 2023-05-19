package ch.rmy.android.http_shortcuts.activities.execute

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

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
        dialogResult?.cancel()
        dialogResult = null
    }

    fun onDialogResult(result: Any) {
        _dialogState.value = null
        dialogResult?.complete(result)
        dialogResult = null
    }
}
