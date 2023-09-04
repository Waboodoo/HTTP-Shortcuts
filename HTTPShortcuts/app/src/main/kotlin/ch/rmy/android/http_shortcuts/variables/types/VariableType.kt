package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.Variable

interface VariableType {
    suspend fun resolve(variable: Variable, dialogHandle: DialogHandle): String
}
