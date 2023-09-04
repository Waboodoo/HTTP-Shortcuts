package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import javax.inject.Inject

class WifiIPAction
@Inject
constructor(
    private val networkUtil: NetworkUtil,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext): String? =
        networkUtil.getIPV4Address()
}
