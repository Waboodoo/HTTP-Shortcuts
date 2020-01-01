package ch.rmy.curlcommand

import java.io.Serializable

class CurlCommand private constructor() : Serializable {

    var url = ""
        private set
    var method = METHOD_GET
        private set
    val headers: Map<String, String>
        get() = headersInternal
    private var headersInternal = mutableMapOf<String, String>()
    var data = ""
        private set
    var timeout = 0
        private set
    var username = ""
        private set
    var password = ""
        private set

    class Builder {

        private val curlCommand = CurlCommand()

        fun url(url: String) = also {
            curlCommand.url = url
        }

        fun method(method: String) = also {
            curlCommand.method = method
        }

        fun data(data: String) = also {
            curlCommand.data = curlCommand.data + data
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

        fun build() = curlCommand

    }

    companion object {

        const val METHOD_GET = "GET"

    }

}
