package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import io.reactivex.Single

class WifiIPAction : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<String> =
        Single.fromCallable {
            getIPAddress(executionContext.context)
        }

    private fun getIPAddress(context: Context): String =
        NetworkUtil.getIPV4Address(context)
            ?: NO_RESULT

}