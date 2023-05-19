package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.models.Variable

abstract class BaseVariableType {

    protected abstract suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String

    open fun inject(applicationComponent: ApplicationComponent) {
        // intentionally left blank
    }

    suspend fun resolve(applicationComponent: ApplicationComponent, variable: Variable, dialogHandle: DialogHandle): String {
        inject(applicationComponent)
        return resolveValue(variable, dialogHandle)
    }
}
