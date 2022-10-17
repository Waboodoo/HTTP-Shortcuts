package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toHexString
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

class ToHexStringAction(private val data: ByteArray) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): String =
        data.toHexString()
}
