package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.http.RequestBuilder
import ch.rmy.android.http_shortcuts.http.ResponseFileStorageFactory
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import ch.rmy.android.scripting.JsObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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
                    sessionId = "${executionContext.shortcutId}_${newUUID()}"
                )

                val response = client.newCall(
                    RequestBuilder(method, url)
                        .header(HttpHeaders.CONNECTION, "close")
                        .userAgent(UserAgentProvider.getUserAgent(context))
                        .build()
                )
                    .execute()

                val contentFile = storage.store(response)

                val shortcutResponse = ShortcutResponse(
                    url = url,
                    headers = HttpHeaders.parse(response.headers),
                    statusCode = response.code,
                    contentFile = contentFile,
                    timing = (response.receivedResponseAtMillis - response.sentRequestAtMillis).milliseconds,
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
                        }
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
    )
}
