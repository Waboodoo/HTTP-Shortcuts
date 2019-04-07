package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Variable

interface SyncVariableType {

    fun resolveValue(controller: Controller, variable: Variable): String

}
