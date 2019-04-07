package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Variable

internal class ConstantType : BaseVariableType(), SyncVariableType {

    override fun createEditorFragment() = ConstantEditorFragment()

    override fun resolveValue(controller: Controller, variable: Variable) = variable.value!!

}
