package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.util.Base64
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.HostVerificationConfig
import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.exceptions.ClientCertException
import ch.rmy.android.http_shortcuts.exceptions.InvalidProxyException
import ch.rmy.android.http_shortcuts.exceptions.NoIpAddressException
import com.burgstaller.okhttp.digest.Credentials
import java.net.Authenticator
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt

@Singleton
class HttpClientFactory
@Inject
constructor() {
    private val baseClient = OkHttpClient.Builder()
        .fastFallback(true)
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
        .build()

    fun getClient(
        context: Context,
        clientCertParams: ClientCertParams? = null,
        username: String? = null,
        password: String? = null,
        followRedirects: Boolean = true,
        timeout: Long = 10000,
        ipVersion: IpVersion? = null,
        proxy: ProxyParams? = null,
        cookieJar: CookieJar? = null,
        certificatePins: List<CertificatePin> = emptyList(),
        hostVerificationConfig: HostVerificationConfig = HostVerificationConfig.Default,
    ): OkHttpClient = baseClient.newBuilder()
        .configureTLS(context, hostVerificationConfig, clientCertParams)
        .runIf(username != null && password != null) {
            val authenticator = DigestAuthenticator(Credentials(username, password))
            authenticator(authenticator)
        }
        .addInterceptor(CompressionInterceptor)
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
                    .build(),
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
                    return super.passwordAuthentication
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
                    ),
                )
            } catch (e: IllegalArgumentException) {
                throw InvalidProxyException(e.message!!)
            }
        }
        .runIfNotNull(ipVersion) {
            dns { hostname ->
                when (it) {
                    IpVersion.V4 -> Dns.SYSTEM.lookup(hostname).filterIsInstance<Inet4Address>()
                    IpVersion.V6 -> Dns.SYSTEM.lookup(hostname).filterIsInstance<Inet6Address>()
                }
                    .ifEmpty {
                        throw NoIpAddressException(hostname, it)
                    }
            }
        }
        .build()

    private fun OkHttpClient.Builder.configureTLS(
        context: Context,
        hostVerificationConfig: HostVerificationConfig,
        clientCertParams: ClientCertParams?,
    ): OkHttpClient.Builder =
        run {
            val trustManager = when (hostVerificationConfig) {
                HostVerificationConfig.Default -> Conscrypt.getDefaultX509TrustManager()
                is HostVerificationConfig.SelfSigned -> UnsafeTrustManager(expectedFingerprint = hostVerificationConfig.expectedFingerprint)
                HostVerificationConfig.TrustAll -> UnsafeTrustManager()
            }
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
            .run {
                when (hostVerificationConfig) {
                    HostVerificationConfig.Default -> this
                    is HostVerificationConfig.SelfSigned,
                    HostVerificationConfig.TrustAll,
                    -> {
                        hostnameVerifier { _, _ -> true }
                    }
                }
            }
}
