package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.Variable

class UUIDType : BaseVariableType() {

    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle) =
        UUIDUtils.newUUID()
}
