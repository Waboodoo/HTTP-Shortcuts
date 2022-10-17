package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

class UUIDAction : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): String =
        UUIDUtils.newUUID()
}
