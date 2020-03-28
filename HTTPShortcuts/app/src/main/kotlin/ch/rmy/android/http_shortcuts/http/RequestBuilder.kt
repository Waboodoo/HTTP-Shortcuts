package ch.rmy.android.http_shortcuts.http

import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.http.HttpMethod
import java.net.URLEncoder

class RequestBuilder(private val method: String, url: String) {

    private val requestBuilder = Request.Builder()
        .url(url)

    private var body: String? = null
    private var contentType: String? = null
    private val parameters = mutableMapOf<String, String>()

    fun basicAuth(username: String, password: String) = also {
        requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, Credentials.basic(username, password))
    }

    fun bearerAuth(authToken: String) = also {
        requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
    }

    fun body(body: String) = also {
        this.body = body
    }

    fun parameter(name: String, value: String) = also {
        parameters[name] = value
    }

    fun header(name: String, value: String) = also {
        requestBuilder.addHeader(name, value)
        if (name.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
            contentType = value
        }
    }

    fun contentType(contentType: String?) = also {
        this.contentType = contentType
    }

    fun build(): Request = requestBuilder
        .also {
            it.method(method, if (HttpMethod.permitsRequestBody(method)) {
                getBody().let { body -> RequestBody.create(MediaType.get(contentType ?: DEFAULT_CONTENT_TYPE), body.toByteArray()) }
            } else {
                null
            })
        }
        .build()

    private fun getBody(): String = when {
        contentType?.startsWith("multipart/form-data") == true -> constructFormDataBody()
        contentType?.startsWith("application/x-www-form-urlencoded") == true -> constructFormUrlEncodedBody()
        else -> body
    } ?: ""

    private fun constructFormDataBody(): String =
        StringBuilder("\r\n")
            .apply {
                parameters.entries.forEach { (key, value) ->
                    append("\r\n--$FORM_MULTIPART_BOUNDARY\n")
                    append("Content-Disposition: form-data; name=\"$key\"")
                    append("\r\n\r\n")
                    append(value)
                }
                append("\n--$FORM_MULTIPART_BOUNDARY--\n")
            }
            .toString()

    private fun constructFormUrlEncodedBody(): String =
        parameters.entries
            .joinToString(separator = "&") { (key, value) ->
                URLEncoder.encode(key, PARAMETER_ENCODING) + '=' + URLEncoder.encode(value, PARAMETER_ENCODING)
            }

    companion object {
        const val FORM_MULTIPART_BOUNDARY = "----53014704754052338"
        private const val DEFAULT_CONTENT_TYPE = "text/plain"
        private const val PARAMETER_ENCODING = "UTF-8"
    }

}