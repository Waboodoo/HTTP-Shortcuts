package ch.rmy.android.http_shortcuts.activities.execute

import android.content.Context
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import javax.inject.Inject

class ExecutionFactory
@Inject
constructor(
    private val context: Context,
) {
    fun createExecution(params: ExecutionParams, dialogHandle: DialogHandle): Execution =
        Execution(context, params, dialogHandle)
}
