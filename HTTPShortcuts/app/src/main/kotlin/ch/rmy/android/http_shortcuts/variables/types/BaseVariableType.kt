package ch.rmy.android.http_shortcuts.variables.types

import android.app.Activity
import android.content.Context
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import kotlinx.coroutines.CancellableContinuation

abstract class BaseVariableType {

    protected abstract suspend fun resolveValue(context: Context, variable: VariableModel): String

    open fun inject(applicationComponent: ApplicationComponent) {
        // intentionally left blank
    }

    suspend fun resolve(context: Context, variable: VariableModel): String {
        inject(context.getApplicationComponent())
        return resolveValue(context, variable)
    }

    companion object {

        fun createDialogBuilder(
            activity: Activity,
            variable: VariableModel,
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
