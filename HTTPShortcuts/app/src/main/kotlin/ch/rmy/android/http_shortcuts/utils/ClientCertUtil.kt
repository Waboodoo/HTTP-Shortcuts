package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import android.security.KeyChain

object ClientCertUtil {
    fun promptForAlias(activity: Activity, callback: (String) -> Unit) {
        KeyChain.choosePrivateKeyAlias(activity, { alias ->
            if (alias != null) {
                callback(alias)
            }
        }, null, null, null, -1, null)
    }
}
