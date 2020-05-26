package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.exceptions.InvalidHeaderException
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_BOUNDARY
import ch.rmy.android.http_shortcuts.http.RequestUtil.encode
import ch.rmy.android.http_shortcuts.http.RequestUtil.getMediaType
import ch.rmy.android.http_shortcuts.http.RequestUtil.sanitize
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.http.HttpMethod
import okio.BufferedSink
import okio.Okio
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

    fun body(inputStream: InputStream) = also {
        this.bodyStream = inputStream
    }

    fun parameter(name: String, value: String) = also {
        parameters.add(Parameter.StringParameter(name, value))
    }

    fun fileParameter(name: String, fileName: String, type: String, data: InputStream) = also {
        parameters.add(Parameter.FileParameter(name, fileName, type, data))
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
        class StringParameter(name: String, val value: String) : Parameter(name)
        class FileParameter(name: String, val fileName: String, val type: String, val data: InputStream) : Parameter(name)
    }

    private fun getBody(): RequestBody = when {
        contentType?.startsWith("multipart/form-data") == true -> constructFormDataBody()
        contentType?.startsWith("application/x-www-form-urlencoded") == true -> constructBodyFromString(constructFormUrlEncodedBody())
        bodyStream != null -> constructBodyFromStream(bodyStream!!)
        else -> constructBodyFromString(body ?: "")
    }

    private fun constructBodyFromString(string: String): RequestBody =
        RequestBody.create(getMediaType(contentType), string.toByteArray())

    private fun constructBodyFromStream(stream: InputStream): RequestBody =
        StreamRequestBody(contentType, stream)

    private fun constructFormDataBody(): RequestBody =
        object : RequestBody() {
            override fun contentType(): MediaType? =
                getMediaType(contentType)

            override fun writeTo(sink: BufferedSink) {
                sink.apply {
                    writeUtf8("\r\n")
                    parameters.forEach { parameter ->
                        writeUtf8("\r\n--$FORM_MULTIPART_BOUNDARY\n")
                        when (parameter) {
                            is Parameter.StringParameter -> {
                                writeUtf8("Content-Disposition: form-data; name=\"${sanitize(parameter.name)}\"")
                                writeUtf8("\r\n\r\n")
                                writeUtf8(parameter.value)
                            }
                            is Parameter.FileParameter -> {
                                writeUtf8("Content-Disposition: form-data; name=\"${sanitize(parameter.name)}\"; filename=\"${sanitize(parameter.fileName)}\"")
                                writeUtf8("\r\n")
                                writeUtf8("Content-Type: ${parameter.type}")
                                writeUtf8("\r\n\r\n")
                                writeAll(Okio.source(parameter.data))
                            }
                        }
                    }
                    writeUtf8("\n--$FORM_MULTIPART_BOUNDARY--\n")
                }
            }
        }

    private fun constructFormUrlEncodedBody(): String =
        parameters
            .filterIsInstance<Parameter.StringParameter>()
            .joinToString(separator = "&") { parameter ->
                encode(parameter.name) + '=' + encode(parameter.value)
            }

}