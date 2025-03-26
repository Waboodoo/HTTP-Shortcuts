package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.framework.extensions.toCharset
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_URLENCODE_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.ResponseFileStorageFactory
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.http.buildRequest
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import ch.rmy.android.scripting.JsObject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendHttpRequestAction
@Inject
constructor(
    @ApplicationContext
    private val context: Context,
    private val httpClientFactory: HttpClientFactory,
    private val responseFileStorageFactory: ResponseFileStorageFactory,
) : Action<SendHttpRequestAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): JsObject =
        try {
            val (response, shortcutResponse) = withContext(Dispatchers.IO) {
                val client = httpClientFactory.getClient(context)
                val storage = responseFileStorageFactory.create(
                    sessionId = "${executionContext.shortcutId}_${newUUID()}",
                )

                val request = buildRequest(method, url) {
                    header(HttpHeaders.CONNECTION, "close")
                    if (body != null) {
                        body(body)
                    } else if (formData != null) {
                        contentType(FORM_URLENCODE_CONTENT_TYPE)
                        formData.forEach { (key, value) ->
                            parameter(key, value)
                        }
                    }
                    headers?.forEach { (key, value) ->
                        header(key, value)
                    }
                    userAgent(UserAgentProvider.getUserAgent(context))
                }
                val response = client.newCall(request)
                    .execute()

                val contentFile = storage.store(response)

                val shortcutResponse = ShortcutResponse(
                    url = url,
                    headers = HttpHeaders.parse(response.headers),
                    statusCode = response.code,
                    contentFile = contentFile,
                    timing = (response.receivedResponseAtMillis - response.sentRequestAtMillis).milliseconds,
                    charsetOverride = charsetOverride?.toCharset(),
                )
                (response to shortcutResponse)
            }

            executionContext.scriptingEngine.buildJsObject {
                property("status", if (response.isSuccessful) "success" else "httpError")
                objectProperty("response") {
                    property(
                        "body",
                        try {
                            shortcutResponse.getContentAsString(context)
                        } catch (_: ResponseTooLargeException) {
                            ""
                        },
                    )
                    property("headers", shortcutResponse.headersAsMultiMap)
                    property("cookies", shortcutResponse.cookiesAsMultiMap)
                    property("statusCode", shortcutResponse.statusCode)
                }
            }
        } catch (e: IOException) {
            executionContext.scriptingEngine.buildJsObject {
                property("status", "networkError")
                property("networkError", e.message)
                property("response", null as String?)
            }
        }

    data class Params(
        val url: String,
        val method: String,
        val body: String?,
        val headers: Map<String, String>?,
        val formData: Map<String, String>?,
        val charsetOverride: String?,
    )
}
