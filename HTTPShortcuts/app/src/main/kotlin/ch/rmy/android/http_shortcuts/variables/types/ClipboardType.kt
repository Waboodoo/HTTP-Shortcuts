package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClipboardType
@Inject
constructor(
    private val clipboardUtil: ClipboardUtil,
) : VariableType {
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle) =
        withContext(Dispatchers.Main) {
            clipboardUtil.getFromClipboard()
                ?.toString()
                .orEmpty()
        }
}
