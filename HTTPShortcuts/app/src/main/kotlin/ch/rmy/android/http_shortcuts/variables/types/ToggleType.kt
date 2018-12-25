package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable

internal class ToggleType : BaseVariableType(), SyncVariableType {

    override fun resolveValue(controller: Controller, variable: Variable): String {
        if (variable.options!!.isEmpty()) {
            return ""
        }
        val previousIndex = variable.value?.toIntOrNull() ?: 0
        val index = (previousIndex + 1) % variable.options!!.size
        controller.setVariableValue(variable.id, index.toString()).subscribe()
        return variable.options!![index]!!.value
    }

    override fun createEditorFragment() = ToggleEditorFragment()

}
