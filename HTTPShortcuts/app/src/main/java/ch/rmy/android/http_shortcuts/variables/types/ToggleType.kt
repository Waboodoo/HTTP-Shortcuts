package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable

internal class ToggleType : BaseVariableType(), SyncVariableType {

    override fun resolveValue(controller: Controller, variable: Variable): String {
        if (variable.options!!.isEmpty()) {
            return ""
        }
        val previousIndex = try {
            Integer.valueOf(variable.value)!!
        } catch (e: NumberFormatException) {
            0
        }

        var index = previousIndex + 1
        if (index >= variable.options!!.size) {
            index = 0
        }
        controller.setVariableValue(variable, index.toString())
        return variable.options!![index]!!.value
    }

    override fun createEditorFragment() = ToggleEditorFragment()

}
