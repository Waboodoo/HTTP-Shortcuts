package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable

interface SyncVariableType {

    fun resolveValue(controller: Controller, variable: Variable): String

}
