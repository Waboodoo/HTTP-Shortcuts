package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject

class ConstantType
@Inject
constructor() : VariableType {
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle) =
        variable.realValue.orEmpty()
}
