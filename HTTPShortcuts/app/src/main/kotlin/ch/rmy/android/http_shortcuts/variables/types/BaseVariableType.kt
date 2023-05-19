package ch.rmy.android.http_shortcuts.variables.types

import android.app.Activity
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import kotlinx.coroutines.CancellableContinuation

abstract class BaseVariableType {

    protected abstract suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String

    open fun inject(applicationComponent: ApplicationComponent) {
        // intentionally left blank
    }

    suspend fun resolve(applicationComponent: ApplicationComponent, variable: Variable, dialogHandle: DialogHandle): String {
        inject(applicationComponent)
        return resolveValue(variable, dialogHandle)
    }

    companion object {

        @Deprecated("REMOVE THIS")
        fun createDialogBuilder(
            activity: Activity,
            variable: Variable,
            continuation: CancellableContinuation<String>,
        ) =
            DialogBuilder(activity)
                .runIf(variable.title.isNotEmpty()) {
                    title(variable.title)
                }
                .runIf(variable.message.isNotEmpty()) {
                    message(variable.message)
                }
                .dismissListener {
                    continuation.cancel()
                }
    }
}
