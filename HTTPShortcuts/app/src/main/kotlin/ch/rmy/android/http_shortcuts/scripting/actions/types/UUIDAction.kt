package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single

class UUIDAction : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        Single.fromCallable {
            UUIDUtils.newUUID()
        }
}
