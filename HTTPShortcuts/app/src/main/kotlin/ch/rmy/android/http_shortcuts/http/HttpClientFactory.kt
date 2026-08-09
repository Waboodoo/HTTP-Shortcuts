package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Network
import android.util.Base64
import androidx.collection.LruCache
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.HostVerificationConfig
import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.exceptions.InvalidProxyException
import ch.rmy.android.http_shortcuts.exceptions.NoIpAddressException
import ch.rmy.android.http_shortcuts.utils.SSLUtil.getKeyManagers
import ch.rmy.android.http_shortcuts.utils.SSLUtil.getTrustManager
import com.burgstaller.okhttp.digest.Credentials
import java.net.Authenticator
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.OkHttpClient

@Singleton
class HttpClientFactory
@Inject
constructor() {
    private val baseClient = OkHttpClient.Builder()
        .fastFallback(true)
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
        .build()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var cleanupJob: Job? = null
    private val cache = LruCache<CacheKey, OkHttpClient>(maxSize = 3)

    private data class CacheKey(
        val clientCertParams: ClientCertParams?,
        val username: String?,
        val password: String?,
        val followRedirects: Boolean,
        val timeout: Long,
        val ipVersion: IpVersion?,
        val proxy: ProxyParams?,
        val cookieJar: CookieJar?,
        val certificatePins: List<CertificatePin>,
        val hostVerificationConfig: HostVerificationConfig,
        val network: Network?,
    )

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
        userAgent: String? = null,
        network: Network? = null,
    ): OkHttpClient {
        val cacheKey = CacheKey(
            clientCertParams = clientCertParams,
            username = username,
            password = password,
            followRedirects = followRedirects,
            timeout = timeout,
            ipVersion = ipVersion,
            proxy = proxy,
            cookieJar = cookieJar,
            certificatePins = certificatePins,
            hostVerificationConfig = hostVerificationConfig,
            network = network,
        )
        val cachedClient = cache[cacheKey]
        if (cachedClient != null) {
            scheduleCacheCleanup()
            return cachedClient
        }

        val client = baseClient.newBuilder()
            .configureTLS(context, hostVerificationConfig, clientCertParams)
            .runIf(username != null && password != null) {
                val authenticator = DigestAuthenticator(Credentials(username, password))
                authenticator(authenticator)
            }
            .addInterceptor(CompressionInterceptor)
            .runIfNotNull(userAgent) { userAgent ->
                addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().header(HttpHeaders.USER_AGENT, userAgent).build())
                }
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
                        .build(),
                )
            }
            .runIfNotNull(cookieJar) {
                cookieJar(it)
            }
            .runIfNotNull(proxy) {
                Authenticator.setDefault(
                    object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            if (it.host.equals(requestingHost, ignoreCase = true) && it.port == requestingPort) {
                                return PasswordAuthentication(it.username, it.password.toCharArray())
                            }
                            return super.passwordAuthentication
                        }
                    },
                )
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
            .runIfNotNull(network) { network ->
                // Bind both TCP (socketFactory) and DNS (getAllByName) to the specified network.
                // The ipVersion filter is replicated here so it still applies when a network is bound.
                socketFactory(network.socketFactory)
                dns { hostname ->
                    network.getAllByName(hostname).toList()
                        .runIfNotNull(ipVersion) { ipVersion ->
                            filter {
                                when (ipVersion) {
                                    IpVersion.V4 -> it is Inet4Address
                                    IpVersion.V6 -> it is Inet6Address
                                }
                            }
                                .ifEmpty { throw NoIpAddressException(hostname, ipVersion) }
                        }
                }
            }
            .build()
        cache.put(cacheKey, client)
        scheduleCacheCleanup()

        return client
    }

    private fun scheduleCacheCleanup() {
        cleanupJob?.cancel()
        cleanupJob = coroutineScope.launch {
            delay(CACHE_CLEAR_TIMEOUT)
            cache.evictAll()
        }
    }

private fun OkHttpClient.Builder.configureTLS(
    context: Context,
    hostVerificationConfig: HostVerificationConfig,
    clientCertParams: ClientCertParams?,
): OkHttpClient.Builder =
    run {
        val trustManager = hostVerificationConfig.getTrustManager()
        val sslContext = SSLContext.getInstance("TLSv1.2", "Conscrypt")

        val keyManagers = clientCertParams?.getKeyManagers(context)
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

    companion object {
        private val CACHE_CLEAR_TIMEOUT = 15.seconds
    }
}
