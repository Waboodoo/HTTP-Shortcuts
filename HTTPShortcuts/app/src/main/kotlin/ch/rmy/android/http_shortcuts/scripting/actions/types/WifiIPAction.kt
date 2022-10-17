package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.NetworkUtil

class WifiIPAction : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): String? =
        NetworkUtil.getIPV4Address(executionContext.context)
}
