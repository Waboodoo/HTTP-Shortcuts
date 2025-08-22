package ch.rmy.android.http_shortcuts.activities.execute

import ch.rmy.android.framework.extensions.logInfo
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
        logInfo("Showing dialog")
        dialogResult?.cancel()
        val dialogResult = CompletableDeferred<Any>()
        this.dialogResult = dialogResult
        _dialogState.value = dialogState
        logInfo("Awaiting dialog result")
        val result = dialogResult.await() as T
        logInfo("Dialog result awaiting done")
        return result
    }

    fun onDialogDismissed() {
        logInfo("Dismissing dialog")
        _dialogState.value = null
        val dialogResult = dialogResult
        this.dialogResult = null
        dialogResult?.cancel(DialogCancellationException())
        logInfo("Dialog dismissed")
    }

    fun onDialogResult(result: Any) {
        logInfo("Dialog result received")
        _dialogState.value = null
        val dialogResult = dialogResult
        this.dialogResult = null
        dialogResult?.complete(result)
        logInfo("Dialog result submitted")
    }
}
