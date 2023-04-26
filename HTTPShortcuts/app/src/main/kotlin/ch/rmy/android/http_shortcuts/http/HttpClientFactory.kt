package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.util.Base64
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.exceptions.ClientCertException
import ch.rmy.android.http_shortcuts.exceptions.InvalidProxyException
import com.burgstaller.okhttp.digest.Credentials
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class HttpClientFactory
@Inject
constructor() {

    fun getClient(
        context: Context,
        clientCertParams: ClientCertParams? = null,
        acceptAllCertificates: Boolean = false,
        username: String? = null,
        password: String? = null,
        followRedirects: Boolean = true,
        timeout: Long = 10000,
        proxy: ProxyParams? = null,
        cookieJar: CookieJar? = null,
        certificatePins: List<CertificatePin> = emptyList(),
    ): OkHttpClient =
        (
            if (acceptAllCertificates) {
                createUnsafeOkHttpClientBuilder()
            } else {
                createDefaultOkHttpClientBuilder(context, clientCertParams)
            }
            )
            .runIf(username != null && password != null) {
                val authenticator = DigestAuthenticator(Credentials(username, password))
                authenticator(authenticator)
            }
            .followRedirects(followRedirects)
            .followSslRedirects(followRedirects)
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .runIf(certificatePins.isNotEmpty()) {
                certificatePinner(
                    CertificatePinner.Builder()
                        .runFor(certificatePins) { pin ->
                            val hash = Base64.encodeToString(pin.hash, Base64.NO_WRAP)
                            val prefix = if (pin.isSha256) "sha256" else "sha1"
                            add(pin.pattern, "$prefix/$hash")
                        }
                        .build()
                )
            }
            .runIfNotNull(cookieJar) {
                cookieJar(it)
            }
            .runIfNotNull(proxy) {
                Authenticator.setDefault(object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        if (it.host.equals(requestingHost, ignoreCase = true) && it.port == requestingPort) {
                            return PasswordAuthentication(it.username, it.password.toCharArray())
                        }
                        return super.getPasswordAuthentication()
                    }
                })
                try {
                    proxy(
                        Proxy(
                            when (it.type) {
                                ProxyType.HTTP -> Proxy.Type.HTTP
                                ProxyType.SOCKS -> Proxy.Type.SOCKS
                            },
                            InetSocketAddress(it.host, it.port),
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    throw InvalidProxyException(e.message!!)
                }
            }
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
