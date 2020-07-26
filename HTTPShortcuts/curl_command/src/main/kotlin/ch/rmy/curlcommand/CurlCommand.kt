package ch.rmy.curlcommand

import java.io.Serializable
import java.net.URLEncoder

class CurlCommand private constructor() : Serializable {

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
    var isFormData: Boolean = false
        private set
    var proxyHost: String = ""
        private set
    var proxyPort: Int = 0
        private set

    class Builder {

        private val curlCommand = CurlCommand()
        private var forceGet = false

        fun url(url: String) = also {
            curlCommand.url = if (url.startsWith("http:", ignoreCase = true) || url.startsWith("https:", ignoreCase = true)) {
                url
            } else {
                "http://$url"
            }
        }

        fun method(method: String) = also {
            curlCommand.method = method.toUpperCase()
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
            method(CurlCommand.METHOD_GET)
            forceGet = true
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

        private fun encode(text: String): String =
            URLEncoder.encode(text, PARAMETER_ENCODING)

    }

}
