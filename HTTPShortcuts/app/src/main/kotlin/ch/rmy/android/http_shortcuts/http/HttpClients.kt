package ch.rmy.android.http_shortcuts.http


import android.content.Context
import ch.rmy.android.http_shortcuts.exceptions.ClientCertException
import ch.rmy.android.http_shortcuts.exceptions.InvalidProxyException
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapIf
import com.burgstaller.okhttp.digest.Credentials
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.ConnectionSpec
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

internal object HttpClients {

    fun getClient(
        context: Context,
        clientCertAlias: String? = null,
        acceptAllCertificates: Boolean = false,
        username: String? = null,
        password: String? = null,
        followRedirects: Boolean = true,
        timeout: Long = 10000,
        proxyHost: String? = null,
        proxyPort: Int? = null,
        cookieJar: CookieJar? = null,
    ): OkHttpClient =
        (if (acceptAllCertificates) {
            createUnsafeOkHttpClientBuilder()
        } else {
            createDefaultOkHttpClientBuilder(context, clientCertAlias)
        })
            .mapIf(username != null && password != null) {
                val authenticator = DigestAuthenticator(Credentials(username, password))
                it.authenticator(authenticator)
            }
            .followRedirects(followRedirects)
            .followSslRedirects(followRedirects)
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .mapIf(cookieJar != null) {
                it.cookieJar(cookieJar!!)
            }
            .mapIf(proxyHost != null && proxyPort != null) {
                try {
                    it.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost!!, proxyPort!!)))
                } catch (e: IllegalArgumentException) {
                    throw InvalidProxyException(e.message!!)
                }
            }
            .addNetworkInterceptor(StethoInterceptor())
            .build()

    private fun createDefaultOkHttpClientBuilder(context: Context, clientCertAlias: String?) = OkHttpClient.Builder()
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
        .run {
            val trustManager = Conscrypt.getDefaultX509TrustManager()
            val sslContext = SSLContext.getInstance("TLS", "Conscrypt")

            val keyManager = try {
                clientCertAlias?.let {
                    arrayOf(ClientCertKeyManager.getClientCertKeyManager(context, it))
                }
            } catch (e: Throwable) {
                logException(e)
                throw ClientCertException()
            }

            sslContext.init(keyManager, arrayOf(trustManager), null)
            sslSocketFactory(TLSEnabledSSLSocketFactory(sslContext.socketFactory), trustManager)
        }

    private fun createUnsafeOkHttpClientBuilder(): OkHttpClient.Builder {
        val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0])
            .hostnameVerifier { _, _ -> true }
    }

}
