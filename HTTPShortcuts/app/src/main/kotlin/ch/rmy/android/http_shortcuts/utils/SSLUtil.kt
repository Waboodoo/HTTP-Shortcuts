package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.http_shortcuts.data.enums.HostVerificationConfig
import ch.rmy.android.http_shortcuts.http.UnsafeTrustManager
import javax.net.ssl.X509TrustManager
import org.conscrypt.Conscrypt

object SSLUtil {
    fun HostVerificationConfig.getTrustManager(): X509TrustManager =
        when (this) {
            HostVerificationConfig.Default -> Conscrypt.getDefaultX509TrustManager()
            is HostVerificationConfig.SelfSigned -> UnsafeTrustManager(expectedFingerprint)
            HostVerificationConfig.TrustAll -> UnsafeTrustManager()
        }
}
