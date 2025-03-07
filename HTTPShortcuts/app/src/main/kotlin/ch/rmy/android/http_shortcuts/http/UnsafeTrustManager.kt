package ch.rmy.android.http_shortcuts.http

import android.annotation.SuppressLint
import ch.rmy.android.framework.extensions.toChunkedHexString
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@SuppressLint("CustomX509TrustManager")
class UnsafeTrustManager(private val expectedFingerprint: ByteArray? = null) : X509TrustManager {
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        if (expectedFingerprint == null) {
            return
        }

        val certificate = chain.firstOrNull()
            ?: throw CertificateException("No certificate found in trust chain")
        val algorithm = if (expectedFingerprint.size == 32) "SHA-256" else "SHA-1"
        val fingerprint = certificate.getFingerprint(algorithm)
            ?: throw CertificateException("Failed to read $algorithm fingerprint")

        if (!fingerprint.contentEquals(expectedFingerprint)) {
            throw CertificateException(
                "Provided certificate did not match expected $algorithm fingerprint.\n" +
                    "Expected ${expectedFingerprint.toChunkedHexString()}\nbut was ${fingerprint.toChunkedHexString()}",
            )
        }
    }

    private fun X509Certificate.getFingerprint(algorithm: String): ByteArray? =
        try {
            MessageDigest.getInstance(algorithm)
                .apply {
                    update(encoded)
                }
                .digest()
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: CertificateEncodingException) {
            null
        }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}
