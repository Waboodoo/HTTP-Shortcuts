package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.data.models.VariableModel

class ConstantType : BaseVariableType() {

    override suspend fun resolveValue(variable: VariableModel) =
        variable.value.orEmpty()
}
