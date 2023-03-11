package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.data.models.Variable

class ConstantType : BaseVariableType() {

    override suspend fun resolveValue(variable: Variable) =
        variable.value.orEmpty()
}
