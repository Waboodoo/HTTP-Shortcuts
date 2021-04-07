package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.security.KeyChain
import okhttp3.OkHttpClient
import java.net.Socket
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509KeyManager
import javax.net.ssl.X509TrustManager

class ClientCertKeyManager(
    private val alias: String,
    private val certChain: Array<X509Certificate>,
    private val privateKey: PrivateKey,
) : X509KeyManager {
    override fun chooseClientAlias(keyType: Array<String>, issuers: Array<Principal>, socket: Socket) =
        alias

    override fun getCertificateChain(alias: String): Array<X509Certificate>? =
        if (alias == this.alias) certChain else null

    override fun getPrivateKey(alias: String): PrivateKey? =
        if (alias == this.alias) privateKey else null

    override fun chooseServerAlias(keyType: String, issuers: Array<Principal>, socket: Socket): String {
        throw UnsupportedOperationException()
    }

    override fun getClientAliases(keyType: String, issuers: Array<Principal>): Array<String> {
        throw UnsupportedOperationException()
    }

    override fun getServerAliases(keyType: String, issuers: Array<Principal>): Array<String> {
        throw UnsupportedOperationException()
    }

    companion object {
        fun applyToOkHttpClient(builder: OkHttpClient.Builder, context: Context, alias: String): OkHttpClient.Builder {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(arrayOf(getClientCertKeyManager(context, alias)), null, null)
            return builder.sslSocketFactory(sslContext.socketFactory, getDefaultTrustManager())
        }

        private fun getDefaultTrustManager(): X509TrustManager {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            return (trustManagerFactory.trustManagers.singleOrNull() as? X509TrustManager)
                ?: throw IllegalStateException("Failed to get default trust manager")
        }

        private fun getClientCertKeyManager(context: Context, alias: String): ClientCertKeyManager {
            val certChain = KeyChain.getCertificateChain(context, alias)
            val privateKey = KeyChain.getPrivateKey(context, alias)
            if (certChain == null || privateKey == null) {
                throw CertificateException("Can't access certificate from keystore")
            }
            return ClientCertKeyManager(alias, certChain, privateKey)
        }
    }
}
