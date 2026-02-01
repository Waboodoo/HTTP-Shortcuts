package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.HostVerificationConfig
import ch.rmy.android.http_shortcuts.exceptions.ClientCertException
import ch.rmy.android.http_shortcuts.http.ClientCertKeyManager
import ch.rmy.android.http_shortcuts.http.UnsafeTrustManager
import java.security.KeyStore
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.X509TrustManager
import org.conscrypt.Conscrypt

object SSLUtil {
    fun HostVerificationConfig.getTrustManager(): X509TrustManager =
        when (this) {
            HostVerificationConfig.Default -> Conscrypt.getDefaultX509TrustManager()
            is HostVerificationConfig.SelfSigned -> UnsafeTrustManager(expectedFingerprint)
            HostVerificationConfig.TrustAll -> UnsafeTrustManager()
        }

    fun ClientCertParams.getKeyManagers(context: Context): Array<out KeyManager> =
        when (this) {
            is ClientCertParams.Alias -> {
                try {
                    arrayOf(ClientCertKeyManager.getClientCertKeyManager(context, alias))
                } catch (e: Throwable) {
                    logException(e)
                    throw ClientCertException()
                }
            }
            is ClientCertParams.File -> {
                val keyStore = KeyStore.getInstance("PKCS12")
                context.openFileInput(fileName).use {
                    keyStore.load(it, password.toCharArray())
                }
                KeyManagerFactory.getInstance("X509")
                    .apply {
                        init(keyStore, password.toCharArray())
                    }
                    .keyManagers
            }
        }
}
