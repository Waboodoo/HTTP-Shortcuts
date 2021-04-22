package ch.rmy.android.http_shortcuts.http

import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class TLSEnabledSSLSocketFactory(private val sslSocketFactory: SSLSocketFactory) : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> =
        sslSocketFactory.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> =
        sslSocketFactory.supportedCipherSuites

    override fun createSocket() =
        withTLS(sslSocketFactory.createSocket())

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean) =
        withTLS(sslSocketFactory.createSocket(s, host, port, autoClose))

    override fun createSocket(host: String, port: Int) =
        withTLS(sslSocketFactory.createSocket(host, port))

    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int) =
        withTLS(sslSocketFactory.createSocket(host, port, localHost, localPort))

    override fun createSocket(host: InetAddress, port: Int) =
        withTLS(sslSocketFactory.createSocket(host, port))

    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int) =
        withTLS(sslSocketFactory.createSocket(address, port, localAddress, localPort))

    private fun withTLS(socket: Socket) = socket.apply {
        (socket as? SSLSocket)?.enabledProtocols = arrayOf("TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3")
    }
}
