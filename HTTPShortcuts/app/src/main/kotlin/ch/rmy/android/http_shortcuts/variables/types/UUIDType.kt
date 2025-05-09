package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import javax.inject.Inject

class UUIDType
@Inject
constructor() : VariableType {
    override suspend fun resolve(variable: GlobalVariable, dialogHandle: DialogHandle) =
        UUIDUtils.newUUID()
}
