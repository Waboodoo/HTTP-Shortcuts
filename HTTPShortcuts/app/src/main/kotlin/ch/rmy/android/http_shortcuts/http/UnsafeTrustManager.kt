package ch.rmy.android.http_shortcuts.http

import java.security.cert.CertificateException
import javax.net.ssl.X509TrustManager

class UnsafeTrustManager : X509TrustManager {
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
    }

    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()
}
