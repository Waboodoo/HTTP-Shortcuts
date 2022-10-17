package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

class ToStringAction(private val data: ByteArray) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): String =
        String(data)
}
