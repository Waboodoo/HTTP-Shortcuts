package ch.rmy.android.http_shortcuts.http


import com.android.volley.AuthFailureError
import com.android.volley.Header
import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.HttpResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit


class OkHttpStack(private val client: OkHttpClient) : BaseHttpStack() {

    override fun executeRequest(request: Request<*>, additionalHeaders: Map<String, String>): HttpResponse {
        val client = prepareClient(request)
        val okHttpRequest = prepareRequest(request, additionalHeaders)
        val okHttpCall = client.newCall(okHttpRequest)
        val okHttpResponse = okHttpCall.execute()
        return processResponse(okHttpResponse)
    }

    private fun prepareClient(request: Request<*>): OkHttpClient {
        val timeout = request.timeoutMs.toLong()
        return client.newBuilder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .build()
    }

    private fun prepareRequest(request: Request<*>, additionalHeaders: Map<String, String>): okhttp3.Request =
        okhttp3.Request.Builder()
            .url(request.url)
            .also { builder ->
                setHeaders(builder, request, additionalHeaders)
            }
            .also { builder ->
                setConnectionParameters(builder, request)
            }
            .build()

    @Throws(AuthFailureError::class)
    private fun setHeaders(builder: okhttp3.Request.Builder, request: Request<*>, additionalHeaders: Map<String, String>) {
        for ((key, value) in request.headers) {
            builder.addHeader(key, value)
        }
        for ((key, value) in additionalHeaders) {
            builder.addHeader(key, value)
        }
    }

    @Throws(AuthFailureError::class)
    private fun setConnectionParameters(builder: okhttp3.Request.Builder, request: Request<*>) {
        when (request.method) {
            Request.Method.GET -> builder.get()
            Request.Method.DELETE -> builder.delete(createRequestBody(request))
            Request.Method.POST -> builder.post(createRequestBody(request))
            Request.Method.PUT -> builder.put(createRequestBody(request))
            Request.Method.HEAD -> builder.head()
            Request.Method.OPTIONS -> builder.method("OPTIONS", createRequestBody(request))
            Request.Method.TRACE -> builder.method("TRACE", null)
            Request.Method.PATCH -> builder.patch(createRequestBody(request))
            else -> throw IllegalStateException("Unknown method type ${request.method}.")
        }
    }

    @Throws(AuthFailureError::class)
    private fun createRequestBody(r: Request<*>): RequestBody {
        val body = r.body ?: ByteArray(0)
        return body.toRequestBody(r.bodyContentType.toMediaTypeOrNull(), 0, body.size)
    }

    private fun processResponse(response: okhttp3.Response): HttpResponse {
        val responseHeaders = response.headers
            .toMultimap()
            .map { multiHeader ->
                multiHeader.value.map { header -> Header(multiHeader.key, header) }
            }
            .flatten()

        val responseBody = response.body

        return HttpResponse(
            response.code,
            responseHeaders,
            responseBody?.contentLength()?.toInt() ?: -1,
            responseBody?.byteStream()
        )
    }
}