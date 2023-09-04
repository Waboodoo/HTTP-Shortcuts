package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Base64
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class Base64DecodeAction
@Inject
constructor() : Action<Base64DecodeAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): ByteArray =
        try {
            Base64.decode(encoded, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            throw ActionException {
                getString(R.string.error_invalid_base64, encoded)
            }
        }

    data class Params(
        val encoded: String,
    )
}
