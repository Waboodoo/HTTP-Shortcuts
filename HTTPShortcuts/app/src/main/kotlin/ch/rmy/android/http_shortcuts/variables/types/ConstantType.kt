package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.models.VariableModel

class ConstantType : BaseVariableType() {

    override suspend fun resolveValue(context: Context, variable: VariableModel) =
        variable.value.orEmpty()
}
