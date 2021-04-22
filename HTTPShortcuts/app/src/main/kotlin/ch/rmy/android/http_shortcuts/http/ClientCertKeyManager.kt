package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.security.KeyChain
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509KeyManager

class ClientCertKeyManager(
    private val alias: String,
    private val certChain: Array<X509Certificate>,
    private val privateKey: PrivateKey,
) : X509KeyManager {
    override fun chooseClientAlias(keyType: Array<String>, issuers: Array<Principal>?, socket: Socket?) =
        alias

    override fun getCertificateChain(alias: String): Array<X509Certificate>? =
        if (alias == this.alias) certChain else null

    override fun getPrivateKey(alias: String): PrivateKey? =
        if (alias == this.alias) privateKey else null

    override fun chooseServerAlias(keyType: String, issuers: Array<Principal>?, socket: Socket?): String {
        throw UnsupportedOperationException()
    }

    override fun getClientAliases(keyType: String, issuers: Array<Principal>?): Array<String> {
        throw UnsupportedOperationException()
    }

    override fun getServerAliases(keyType: String, issuers: Array<Principal>?): Array<String> {
        throw UnsupportedOperationException()
    }

    companion object {
        fun getClientCertKeyManager(context: Context, alias: String): ClientCertKeyManager {
            val certChain = KeyChain.getCertificateChain(context, alias)
            val privateKey = KeyChain.getPrivateKey(context, alias)
            if (certChain == null || privateKey == null) {
                throw CertificateException("Can't access certificate from keystore")
            }
            return ClientCertKeyManager(alias, certChain, privateKey)
        }
    }
}
