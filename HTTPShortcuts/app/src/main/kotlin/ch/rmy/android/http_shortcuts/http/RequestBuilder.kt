package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.exceptions.InvalidHeaderException
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_URLENCODE_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.encode
import ch.rmy.android.http_shortcuts.http.RequestUtil.getMediaType
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.http.HttpMethod
import java.io.InputStream
import java.net.URISyntaxException

class RequestBuilder(private val method: String, url: String) {

    private val requestBuilder = Request.Builder()
        .also {
            try {
                it.url(url)
            } catch (e: IllegalArgumentException) {
                throw InvalidUrlException(url, e.message)
            } catch (e: URISyntaxException) {
                throw InvalidUrlException(url)
            }
        }

    private var body: String? = null
    private var bodyStream: InputStream? = null
    private var bodyLength: Long? = null
    private var contentType: String? = null
    private val parameters = mutableListOf<Parameter>()

    fun basicAuth(username: String, password: String) = also {
        requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, Credentials.basic(username, password))
    }

    fun bearerAuth(authToken: String) = also {
        requestBuilder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
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
        length: Long?
    ) = also {
        parameters.add(Parameter.FileParameter(
            name,
            fileName,
            type,
            data,
            length
        ))
    }

    fun header(name: String, value: String) = also {
        try {
            requestBuilder.addHeader(name, value)
        } catch (e: IllegalArgumentException) {
            throw InvalidHeaderException("$name: $value")
        }
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
                getBody()
            } else {
                null
            })
        }
        .build()

    sealed class Parameter(val name: String) {
        class StringParameter(
            name: String,
            val value: String
        ) : Parameter(name)

        class FileParameter(
            name: String,
            val fileName: String,
            val type: String,
            val data: InputStream,
            val length: Long?
        ) : Parameter(name)
    }

    private fun getBody(): RequestBody = when {
        contentType == FORM_MULTIPART_CONTENT_TYPE -> FormMultipartRequestBody(parameters)
        contentType == FORM_URLENCODE_CONTENT_TYPE -> constructBodyFromString(constructFormUrlEncodedBody())
        bodyStream != null -> constructBodyFromStream(bodyStream!!, bodyLength)
        else -> constructBodyFromString(body ?: "")
    }

    private fun constructBodyFromString(string: String): RequestBody =
        RequestBody.create(getMediaType(contentType), string.toByteArray())

    private fun constructBodyFromStream(stream: InputStream, length: Long?): RequestBody =
        StreamRequestBody(contentType, stream, length)

    private fun constructFormUrlEncodedBody(): String =
        parameters
            .filterIsInstance<Parameter.StringParameter>()
            .joinToString(separator = "&") { parameter ->
                encode(parameter.name) + '=' + encode(parameter.value)
            }

}