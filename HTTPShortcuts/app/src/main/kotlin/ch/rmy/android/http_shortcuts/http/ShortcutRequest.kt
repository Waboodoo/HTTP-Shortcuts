package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import io.reactivex.SingleEmitter
import okhttp3.Credentials
import org.apache.http.HttpHeaders

internal class ShortcutRequest private constructor(
    method: Int,
    url: String,
    private val emitter: SingleEmitter<ShortcutResponse>
) : Request<ShortcutResponse>(method, url, Response.ErrorListener(emitter::onError)) {

    private val parameters = mutableMapOf<String, String>()
    private val headers = mutableMapOf<String, String>()
    private var bodyContent: String? = null
    private var contentType: String? = null

    init {
        headers[HttpHeaders.CONNECTION] = "close"
        headers[HttpHeaders.USER_AGENT] = UserAgentUtil.userAgent
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? = when {
        contentType?.startsWith("multipart/form-data") == true -> constructFormDataBody().toByteArray()
        contentType?.startsWith("application/x-www-form-urlencoded") == true -> super.getBody() ?: ByteArray(0)
        else -> (bodyContent ?: "").toByteArray()
    }

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> = headers

    override fun getBodyContentType(): String = contentType ?: DEFAULT_CONTENT_TYPE

    public override fun getParams() = parameters

    override fun parseNetworkResponse(response: NetworkResponse): Response<ShortcutResponse> =
        Response.success(ShortcutResponse(url, response.headers, response.statusCode, response.data), null)

    override fun deliverResponse(response: ShortcutResponse) {
        emitter.onSuccess(response)
    }

    private fun constructFormDataBody(): String =
        StringBuilder("\r\n")
            .apply {
                parameters.entries.forEach { entry ->
                    append("\r\n--$FORM_MULTIPART_BOUNDARY\n")
                    append("Content-Disposition: form-data; name=\"${entry.key}\"")
                    append("\r\n\r\n")
                    append(entry.value)
                }
                append("\n--$FORM_MULTIPART_BOUNDARY--\n")
            }.toString()

    internal class Builder(method: String, url: String, emitter: SingleEmitter<ShortcutResponse>) {

        private val request: ShortcutRequest

        init {
            request = ShortcutRequest(getMethod(method), url, emitter)
        }

        private fun getMethod(method: String) = when (method) {
            Shortcut.METHOD_POST -> Method.POST
            Shortcut.METHOD_PUT -> Method.PUT
            Shortcut.METHOD_DELETE -> Method.DELETE
            Shortcut.METHOD_PATCH -> Method.PATCH
            Shortcut.METHOD_OPTIONS -> Method.OPTIONS
            Shortcut.METHOD_HEAD -> Method.HEAD
            Shortcut.METHOD_TRACE -> Method.TRACE
            else -> Method.GET
        }

        fun contentType(contentType: String) = also {
            request.contentType = contentType
        }

        fun basicAuth(username: String, password: String) = also {
            request.headers[HttpHeaders.AUTHORIZATION] = Credentials.basic(username, password)
        }

        fun body(body: String) = also {
            request.bodyContent = body
        }

        fun parameter(key: String, value: String) = also {
            request.parameters[key] = value
        }

        fun header(key: String, value: String) = also {
            if (key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
                request.contentType = value
            }
            request.headers[key] = value
        }

        fun timeout(timeout: Int) = also {
            request.retryPolicy = DefaultRetryPolicy(timeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        }

        fun build() = request

    }

    companion object {

        const val FORM_MULTIPART_BOUNDARY = "----53014704754052338"
        private const val DEFAULT_CONTENT_TYPE = "text/plain"

    }

}
