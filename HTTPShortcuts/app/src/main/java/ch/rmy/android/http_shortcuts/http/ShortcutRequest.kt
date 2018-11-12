package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import ch.rmy.android.http_shortcuts.utils.rejectSafely
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import okhttp3.Credentials
import org.apache.http.HttpHeaders
import org.jdeferred2.Deferred
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

internal class ShortcutRequest private constructor(
        method: Int,
        url: String,
        private val deferred: Deferred<ShortcutResponse, VolleyError, Unit>
) : Request<ShortcutResponse>(method, url, Response.ErrorListener { error -> deferred.rejectSafely(error) }) {

    private val parameters = mutableMapOf<String, String>()
    private val headers = mutableMapOf<String, String>()
    private var bodyContent: String? = null
    private var contentType: String = "text/plain"

    init {
        headers[HttpHeaders.CONNECTION] = "close"
        headers[HttpHeaders.USER_AGENT] = UserAgentUtil.userAgent
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? = when {
        contentType.startsWith("multipart/form-data") -> constructFormDataBody().toByteArray()
        contentType.startsWith("application/x-www-form-urlencoded") -> super.getBody() ?: ByteArray(0)
        else -> (bodyContent ?: "").toByteArray()
    }

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> = headers

    override fun getBodyContentType(): String = contentType

    public override fun getParams() = parameters

    override fun parseNetworkResponse(response: NetworkResponse): Response<ShortcutResponse> =
            Response.success(ShortcutResponse(response.headers, response.statusCode, response.data), null)

    override fun deliverResponse(response: ShortcutResponse) {
        deferred.resolve(response)
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

    val promise: Promise<ShortcutResponse, VolleyError, Unit>
        get() = deferred.promise()

    internal class Builder(method: String, url: String) {

        private val request: ShortcutRequest

        init {
            request = ShortcutRequest(getMethod(method), url, DeferredObject<ShortcutResponse, VolleyError, Unit>())
        }

        private fun getMethod(method: String) = when (method) {
            Shortcut.METHOD_POST -> Request.Method.POST
            Shortcut.METHOD_PUT -> Request.Method.PUT
            Shortcut.METHOD_DELETE -> Request.Method.DELETE
            Shortcut.METHOD_PATCH -> Request.Method.PATCH
            Shortcut.METHOD_OPTIONS -> Request.Method.OPTIONS
            Shortcut.METHOD_HEAD -> Request.Method.HEAD
            Shortcut.METHOD_TRACE -> Request.Method.TRACE
            else -> Request.Method.GET
        }

        fun contentType(contentType: String) = this.also {
            request.contentType = contentType
        }

        fun basicAuth(username: String, password: String) = this.also {
            request.headers[HttpHeaders.AUTHORIZATION] = Credentials.basic(username, password)
        }

        fun body(body: String) = this.also {
            request.bodyContent = body
        }

        fun parameter(key: String, value: String) = this.also {
            request.parameters[key] = value
        }

        fun header(key: String, value: String) = this.also {
            if (key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
                request.contentType = value
            } else {
                request.headers[key] = value
            }
        }

        fun timeout(timeout: Int) = this.also {
            request.retryPolicy = DefaultRetryPolicy(timeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        }

        fun build() = request

    }

    companion object {

        const val FORM_MULTIPART_BOUNDARY = "----53014704754052338"

    }

}
