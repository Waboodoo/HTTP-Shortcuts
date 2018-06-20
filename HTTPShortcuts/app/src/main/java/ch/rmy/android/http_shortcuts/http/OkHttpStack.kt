package ch.rmy.android.http_shortcuts.http

/*
 * Copyright (C) 2016 Eric Cochran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.HttpStack
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.RequestBody
import okhttp3.Response
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.ProtocolVersion
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * [HttpStack] backed by OkHttp 3.
 */

class OkHttpStack(private val client: OkHttpClient) : HttpStack {

    @Throws(IOException::class, AuthFailureError::class)
    override fun performRequest(request: Request<*>, additionalHeaders: Map<String, String>): HttpResponse {
        val clientBuilder = client.newBuilder()
        val timeoutMs = request.timeoutMs
        clientBuilder.connectTimeout(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
        clientBuilder.readTimeout(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
        clientBuilder.writeTimeout(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
        val client = clientBuilder.build()

        val requestBuilder = okhttp3.Request.Builder()
        requestBuilder.url(request.url)

        setHeaders(requestBuilder, request, additionalHeaders)
        setConnectionParameters(requestBuilder, request)

        val okHttpRequest = requestBuilder.build()
        val okHttpCall = client.newCall(okHttpRequest)
        val okHttpResponse = okHttpCall.execute()

        val responseStatus = BasicStatusLine(parseProtocol(okHttpResponse.protocol()), okHttpResponse.code(),
                okHttpResponse.message())
        val response = BasicHttpResponse(responseStatus)
        response.entity = getEntity(okHttpResponse)

        val responseHeaders = okHttpResponse.headers()
        var i = 0
        val len = responseHeaders.size()
        while (i < len) {
            response.addHeader(BasicHeader(responseHeaders.name(i), responseHeaders.value(i)))
            i++
        }

        return response
    }

    private fun parseProtocol(p: Protocol) = when (p) {
        Protocol.HTTP_1_0 -> ProtocolVersion("HTTP", 1, 0)
        Protocol.HTTP_1_1 -> ProtocolVersion("HTTP", 1, 1)
        Protocol.HTTP_2 -> ProtocolVersion("HTTP", 2, 0)
        else -> {
            throw IllegalAccessError("Unknown protocol: " + p)
        }
    }

    @Throws(IOException::class)
    private fun getEntity(response: Response): HttpEntity {
        val entity = BasicHttpEntity()
        val body = response.body()
        entity.content = body!!.byteStream()
        entity.contentLength = body.contentLength()
        entity.setContentEncoding(response.header("Content-Encoding"))
        if (body.contentType() != null) {
            entity.setContentType(body.contentType()!!.type())
        }
        return entity
    }

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
            Request.Method.DELETE -> builder.delete()
            Request.Method.POST -> builder.post(createRequestBody(request))
            Request.Method.PUT -> builder.put(createRequestBody(request))
            Request.Method.HEAD -> builder.head()
            Request.Method.OPTIONS -> builder.method("OPTIONS", null)
            Request.Method.TRACE -> builder.method("TRACE", null)
            Request.Method.PATCH -> builder.patch(createRequestBody(request))
            else -> throw IllegalStateException("Unknown method type.")
        }
    }

    @Throws(AuthFailureError::class)
    private fun createRequestBody(r: Request<*>): RequestBody? {
        val body = r.body ?: return null
        return RequestBody.create(MediaType.parse(r.bodyContentType), body)
    }
}