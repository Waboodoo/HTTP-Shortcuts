package ch.rmy.android.http_shortcuts.http


import ch.rmy.android.http_shortcuts.utils.mapIf
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

internal object HttpClients {

    fun getClient(acceptAllCertificates: Boolean, username: String?, password: String?): OkHttpClient =
            (if (acceptAllCertificates) createUnsafeOkHttpClientBuilder() else createDefaultOkHttpClientBuilder())
                    .mapIf(username != null && password != null) {
                        val authenticator = DigestAuthenticator(Credentials(username, password))
                        it.authenticator(authenticator)
                    }
                    .addNetworkInterceptor(StethoInterceptor())
                    .build()

    private fun createDefaultOkHttpClientBuilder() = OkHttpClient.Builder()

    private fun createUnsafeOkHttpClientBuilder(): OkHttpClient.Builder {
        try {
            val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustAllCerts[0])
                    .hostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

}
