package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Base64
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

class Base64EncodeAction(private val text: ByteArray) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): String =
        Base64.encodeToString(text, Base64.NO_WRAP)
}
