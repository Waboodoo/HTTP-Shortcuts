package ch.rmy.android.http_shortcuts.http

import android.content.Context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.extensions.mapIfNotNull
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.exceptions.ClientCertException
import ch.rmy.android.http_shortcuts.exceptions.InvalidProxyException
import com.burgstaller.okhttp.digest.Credentials
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.ConnectionSpec
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

internal object HttpClients {

    fun getClient(
        context: Context,
        clientCertParams: ClientCertParams? = null,
        acceptAllCertificates: Boolean = false,
        username: String? = null,
        password: String? = null,
        followRedirects: Boolean = true,
        timeout: Long = 10000,
        proxyHost: String? = null,
        proxyPort: Int? = null,
        cookieJar: CookieJar? = null,
    ): OkHttpClient =
        (
            if (acceptAllCertificates) {
                createUnsafeOkHttpClientBuilder()
            } else {
                createDefaultOkHttpClientBuilder(context, clientCertParams)
            }
            )
            .mapIf(username != null && password != null) {
                val authenticator = DigestAuthenticator(Credentials(username, password))
                authenticator(authenticator)
            }
            .followRedirects(followRedirects)
            .followSslRedirects(followRedirects)
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .mapIfNotNull(cookieJar) {
                cookieJar(it)
            }
            .mapIf(proxyHost != null && proxyPort != null) {
                try {
                    proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost!!, proxyPort!!)))
                } catch (e: IllegalArgumentException) {
                    throw InvalidProxyException(e.message!!)
                }
            }
            .addNetworkInterceptor(StethoInterceptor())
            .build()

    private fun createDefaultOkHttpClientBuilder(context: Context, clientCertParams: ClientCertParams?) = OkHttpClient.Builder()
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
        .run {
            val trustManager = Conscrypt.getDefaultX509TrustManager()
            val sslContext = SSLContext.getInstance("TLS", "Conscrypt")

            val keyManagers = when (clientCertParams) {
                is ClientCertParams.Alias -> {
                    try {
                        arrayOf(ClientCertKeyManager.getClientCertKeyManager(context, clientCertParams.alias))
                    } catch (e: Throwable) {
                        logException(e)
                        throw ClientCertException()
                    }
                }
                is ClientCertParams.File -> {
                    val keyStore = KeyStore.getInstance("PKCS12")
                    context.openFileInput(clientCertParams.fileName).use {
                        keyStore.load(it, clientCertParams.password.toCharArray())
                    }
                    KeyManagerFactory.getInstance("X509")
                        .apply {
                            init(keyStore, clientCertParams.password.toCharArray())
                        }
                        .keyManagers
                }
                else -> null
            }

            sslContext.init(keyManagers, arrayOf(trustManager), null)
            sslSocketFactory(TLSEnabledSSLSocketFactory(sslContext.socketFactory), trustManager)
        }

    private fun createUnsafeOkHttpClientBuilder(): OkHttpClient.Builder {
        val trustAllCerts = arrayOf<X509TrustManager>(UnsafeTrustManager())

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0])
            .hostnameVerifier { _, _ -> true }
    }
}
