package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Base64
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class Base64EncodeAction
@Inject
constructor() : Action<Base64EncodeAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String =
        Base64.encodeToString(text, Base64.NO_WRAP)

    data class Params(
        val text: ByteArray,
    )
}
