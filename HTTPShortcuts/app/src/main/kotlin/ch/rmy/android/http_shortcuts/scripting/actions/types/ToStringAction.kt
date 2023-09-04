package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class ToStringAction
@Inject
constructor() : Action<ToStringAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String =
        String(data)

    data class Params(
        val data: ByteArray,
    )
}
