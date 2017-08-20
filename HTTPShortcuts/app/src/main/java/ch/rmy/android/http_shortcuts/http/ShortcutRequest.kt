package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import com.android.volley.*
import okhttp3.Credentials
import org.apache.http.HttpHeaders
import org.jdeferred.Deferred
import org.jdeferred.Promise
import org.jdeferred.impl.DeferredObject
import java.util.*

internal class ShortcutRequest private constructor(method: Int, url: String, private val deferred: Deferred<ShortcutResponse, VolleyError, Void>) : Request<ShortcutResponse>(method, url, Response.ErrorListener { error -> deferred.reject(error) }) {

    private val parameters = HashMap<String, String>()
    private val headers = HashMap<String, String>()
    private var bodyContent: String? = null
    private var contentType: String? = null

    init {
        headers.put(HttpHeaders.CONNECTION, "close")
        headers.put(HttpHeaders.USER_AGENT, UserAgentUtil.userAgent)
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val regularBody = super.getBody()
        val customBody = bodyContent!!.toByteArray()
        if (regularBody == null) {
            return customBody
        }
        val mergedBody = ByteArray(regularBody.size + customBody.size)

        System.arraycopy(regularBody, 0, mergedBody, 0, regularBody.size)
        System.arraycopy(customBody, 0, mergedBody, regularBody.size, customBody.size)

        return mergedBody

    }

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return headers
    }

    override fun getBodyContentType(): String {
        return contentType ?: super.getBodyContentType()
    }

    public override fun getParams() = parameters

    override fun parseNetworkResponse(response: NetworkResponse): Response<ShortcutResponse> {
        return Response.success(ShortcutResponse(response.headers, response.data), null)
    }

    override fun deliverResponse(response: ShortcutResponse) {
        deferred.resolve(response)
    }

    val promise: Promise<ShortcutResponse, VolleyError, Void>
        get() = deferred.promise()

    internal class Builder(method: String, url: String) {

        private val request: ShortcutRequest

        init {
            request = ShortcutRequest(getMethod(method), url, DeferredObject<ShortcutResponse, VolleyError, Void>())
        }

        private fun getMethod(method: String): Int {
            when (method) {
                Shortcut.METHOD_POST -> return Request.Method.POST
                Shortcut.METHOD_PUT -> return Request.Method.PUT
                Shortcut.METHOD_DELETE -> return Request.Method.DELETE
                Shortcut.METHOD_PATCH -> return Request.Method.PATCH
                Shortcut.METHOD_OPTIONS -> return Request.Method.OPTIONS
                Shortcut.METHOD_HEAD -> return Request.Method.HEAD
                Shortcut.METHOD_TRACE -> return Request.Method.TRACE
                else -> return Request.Method.GET
            }
        }

        fun basicAuth(username: String, password: String): Builder {
            request.headers.put(HttpHeaders.AUTHORIZATION, Credentials.basic(username, password))
            return this
        }

        fun body(body: String): Builder {
            request.bodyContent = body
            return this
        }

        fun parameter(key: String, value: String): Builder {
            request.parameters.put(key, value)
            return this
        }

        fun header(key: String, value: String): Builder {
            if (key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
                request.contentType = value
            } else {
                request.headers.put(key, value)
            }
            return this
        }

        fun timeout(timeout: Int): Builder {
            request.retryPolicy = DefaultRetryPolicy(timeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            return this
        }

        fun build() = request

    }

}
