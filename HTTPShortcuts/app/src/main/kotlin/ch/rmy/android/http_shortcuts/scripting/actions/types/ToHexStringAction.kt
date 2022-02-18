package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toHexString
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single

class ToHexStringAction(private val data: ByteArray) : BaseAction() {
    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        Single.fromCallable {
            data.toHexString()
        }
}
