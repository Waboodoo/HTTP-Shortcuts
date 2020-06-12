package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.net.wifi.WifiManager
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.reactivex.Single

class WIFIIPType : BaseVariableType(), AsyncVariableType {
    override fun resolveValue(context: Context, variable: Variable): Single<String> =
            Single.create<String> { emitter ->
                emitter.onSuccess(
                        integerToStringIP(
                                (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                                        .connectionInfo.ipAddress
                        )
                )
            }.flatMap { resolvedValue ->
                Commons.setVariableValue(variable.id, resolvedValue)
                        .toSingle { resolvedValue }
            }

    override val hasTitle =false
    private fun integerToStringIP(ip: Int): String {
        return (ip shr 0 and 0xFF).toString() + "." +
                (ip shr 8 and 0xFF) + "." +
                (ip shr 16 and 0xFF) + "." +
                (ip shr 24 and 0xFF)
    }
}