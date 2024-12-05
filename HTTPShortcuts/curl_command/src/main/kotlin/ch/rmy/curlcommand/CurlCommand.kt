package ch.rmy.curlcommand

import java.io.Serializable
import java.net.URLEncoder

class CurlCommand internal constructor() : Serializable {

    var url = ""
        private set
    var method = METHOD_GET
        private set
    val headers: Map<String, String>
        get() = headersInternal
    private val headersInternal = mutableMapOf<String, String>()
    val data: List<String>
        get() = dataInternal
    var usesBinaryData: Boolean = false
        private set
    private val dataInternal = mutableListOf<String>()
    var timeout = 0
        private set
    var username = ""
        private set
    var password = ""
        private set
    var isDigestAuth: Boolean = false
        private set
    var isFormData: Boolean = false
        private set
    var proxyHost: String = ""
        private set
    var proxyPort: Int = 0
        private set
    var insecure: Boolean = false
        private set
    var silent: Boolean = false
        private set
    var ipVersion4: Boolean = false
        private set
    var ipVersion6: Boolean = false
        private set

    class Builder {

        private val curlCommand = CurlCommand()
        private var forceGet = false
        private var methodSet = false

        fun url(url: String) = also {
            curlCommand.url = if (url.startsWith("http:", ignoreCase = true) || url.startsWith("https:", ignoreCase = true)) {
                url
            } else {
                "http://$url"
            }
        }

        fun method(method: String) = also {
            methodSet = true
            curlCommand.method = method.uppercase()
        }

        fun methodIfNotYetSet(method: String) = also {
            if (!methodSet) {
                method(method)
            }
        }

        fun data(data: String) = also {
            if (data.isNotEmpty()) {
                curlCommand.dataInternal.add(data)
            }
        }

        fun isFormData() = also {
            curlCommand.isFormData = true
        }

        fun usesBinaryData() = also {
            curlCommand.usesBinaryData = true
        }

        fun addParameter(key: String, value: String) = also {
            curlCommand.dataInternal.add(encode(key) + "=" + encode(value))
        }

        fun addFileParameter(key: String) = also {
            curlCommand.isFormData = true
            curlCommand.dataInternal.add(encode(key) + "=@file")
        }

        fun timeout(timeout: Int) = also {
            curlCommand.timeout = timeout
        }

        fun isDigestAuth() = also {
            curlCommand.isDigestAuth = true
        }

        fun username(username: String) = also {
            curlCommand.username = username
        }

        fun password(password: String) = also {
            curlCommand.password = password
        }

        fun header(key: String, value: String) = also {
            curlCommand.headersInternal[key] = value
        }

        fun proxy(host: String, port: Int) = also {
            curlCommand.proxyHost = host
            curlCommand.proxyPort = port
        }

        fun forceGet() {
            method(METHOD_GET)
            forceGet = true
        }

        fun insecure() = also {
            curlCommand.insecure = true
        }

        fun silent() = also {
            curlCommand.silent = true
        }

        fun ipVersion4() = also {
            curlCommand.ipVersion4 = true
            curlCommand.ipVersion6 = false
        }

        fun ipVersion6() = also {
            curlCommand.ipVersion6 = true
            curlCommand.ipVersion4 = false
        }

        fun build(): CurlCommand {
            if (forceGet) {
                // TODO: This is a naive implementation, which is not generally correct
                val queryString = curlCommand.dataInternal.joinToString("&")
                curlCommand.dataInternal.clear()
                curlCommand.url += if (curlCommand.url.contains("?")) {
                    "&"
                } else {
                    "?"
                } + queryString
            }

            return curlCommand
        }
    }

    companion object {

        private const val PARAMETER_ENCODING = "UTF-8"

        const val METHOD_GET = "GET"

        internal fun encode(text: String): String =
            URLEncoder.encode(text, PARAMETER_ENCODING)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CurlCommand) return false
        if (url != other.url) return false
        if (method != other.method) return false
        if (headersInternal != other.headersInternal) return false
        if (usesBinaryData != other.usesBinaryData) return false
        if (dataInternal != other.dataInternal) return false
        if (timeout != other.timeout) return false
        if (username != other.username) return false
        if (password != other.password) return false
        if (isFormData != other.isFormData) return false
        if (proxyHost != other.proxyHost) return false
        if (proxyPort != other.proxyPort) return false
        if (insecure != other.insecure) return false
        if (silent != other.silent) return false
        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + headersInternal.hashCode()
        result = 31 * result + usesBinaryData.hashCode()
        result = 31 * result + dataInternal.hashCode()
        result = 31 * result + timeout
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + isFormData.hashCode()
        result = 31 * result + proxyHost.hashCode()
        result = 31 * result + proxyPort
        result = 31 * result + insecure.hashCode()
        result = 31 * result + silent.hashCode()
        return result
    }
}
