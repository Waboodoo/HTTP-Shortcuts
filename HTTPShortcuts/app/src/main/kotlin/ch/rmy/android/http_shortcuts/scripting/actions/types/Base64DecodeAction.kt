package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Base64
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

class Base64DecodeAction(private val encoded: String) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): ByteArray =
        try {
            Base64.decode(encoded, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            throw ActionException { context ->
                context.getString(R.string.error_invalid_base64, encoded)
            }
        }
}
