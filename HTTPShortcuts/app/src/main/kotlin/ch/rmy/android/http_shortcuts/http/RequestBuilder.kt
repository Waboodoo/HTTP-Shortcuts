package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.exceptions.InvalidBearerAuthException
import ch.rmy.android.http_shortcuts.exceptions.InvalidHeaderException
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_URLENCODE_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.encode
import ch.rmy.android.http_shortcuts.http.RequestUtil.getMediaType
import java.io.InputStream
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets.UTF_8
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http.HttpMethod

class RequestBuilder(private val method: String, url: String) {

    private val requestBuilder = Request.Builder()
        .also {
            try {
                it.url(url)
            } catch (e: IllegalArgumentException) {
                throw InvalidUrlException(url, e.message)
            } catch (_: URISyntaxException) {
                throw InvalidUrlException(url)
            }
        }

    private var body: String? = null
    private var bodyStream: InputStream? = null
    private var bodyLength: Long? = null
    private var contentType: String? = null
    private var userAgent: String? = null
    private val parameters = mutableListOf<Parameter>()

    fun basicAuth(username: String, password: String) = also {
        requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, Credentials.basic(username, password, UTF_8))
    }

    fun bearerAuth(authToken: String) = also {
        try {
            requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
        } catch (e: IllegalArgumentException) {
            throw InvalidBearerAuthException(authToken)
        }
    }

    fun body(body: String) = also {
        this.body = body
    }

    fun body(inputStream: InputStream, length: Long?) = also {
        this.bodyStream = inputStream
        this.bodyLength = length
    }

    fun parameter(name: String, value: String) = also {
        parameters.add(Parameter.StringParameter(name, value))
    }

    fun fileParameter(
        name: String,
        fileName: String,
        type: String,
        data: InputStream,
        length: Long?,
    ) = also {
        parameters.add(
            Parameter.FileParameter(
                name,
                fileName,
                type,
                data,
                length,
            ),
        )
    }

    fun header(name: String, value: String) = also {
        try {
            requestBuilder.addHeader(name, value)
        } catch (e: IllegalArgumentException) {
            throw InvalidHeaderException("$name: $value")
        }
        when {
            name.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true) -> {
                contentType = value
            }
            name.equals(HttpHeaders.USER_AGENT, ignoreCase = true) -> {
                userAgent = null
            }
        }
    }

    fun userAgent(userAgent: String) = also {
        this.userAgent = userAgent
    }

    fun contentType(contentType: String?) = also {
        this.contentType = contentType
    }

    fun build(): Request = requestBuilder
        .run {
            method(
                method,
                if (HttpMethod.permitsRequestBody(method)) {
                    getBody()
                } else {
                    null
                },
            )
        }
        .runIfNotNull(userAgent) {
            header(HttpHeaders.USER_AGENT, it)
        }
        .build()

    sealed class Parameter(val name: String) {
        class StringParameter(
            name: String,
            val value: String,
        ) : Parameter(name)

        class FileParameter(
            name: String,
            val fileName: String,
            val type: String,
            val data: InputStream,
            val length: Long?,
        ) : Parameter(name)
    }

    private fun getBody(): RequestBody = when {
        contentType == FORM_MULTIPART_CONTENT_TYPE -> FormMultipartRequestBody(parameters)
        contentType?.startsWith(FORM_URLENCODE_CONTENT_TYPE, ignoreCase = true) == true -> constructBodyFromString(
            constructFormUrlEncodedBody()
                .ifEmpty { body ?: "" },
        )
        bodyStream != null -> constructBodyFromStream(bodyStream!!, bodyLength)
        else -> constructBodyFromString(body ?: "")
    }

    private fun constructBodyFromString(string: String): RequestBody =
        string.toByteArray().let { content ->
            content.toRequestBody(getMediaType(contentType), 0, content.size)
        }

    private fun constructBodyFromStream(stream: InputStream, length: Long?): RequestBody =
        StreamRequestBody(contentType, stream, length)

    private fun constructFormUrlEncodedBody(): String =
        parameters
            .filterIsInstance<Parameter.StringParameter>()
            .joinToString(separator = "&") { parameter ->
                encode(parameter.name) + '=' + encode(parameter.value)
            }
}
