package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Base64
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single

class Base64EncodeAction(private val text: ByteArray) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        Single.fromCallable {
            Base64.encodeToString(text, Base64.DEFAULT)
        }
}
