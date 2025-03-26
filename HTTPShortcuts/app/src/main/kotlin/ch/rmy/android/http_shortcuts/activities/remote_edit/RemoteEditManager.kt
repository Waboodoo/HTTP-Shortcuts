package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportMode
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.source

class RemoteEditManager(
    private val context: Context,
    private val client: OkHttpClient,
    private val baseUrl: Uri,
    private val exporter: Exporter,
    private val importer: Importer,
) {

    suspend fun upload(deviceId: String, password: String) {
        withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "remote-edit.json")
            exporter.exportToUri(
                FileUtil.getUriFromFile(context, file),
                format = ExportFormat.LEGACY_JSON,
                excludeVariableValuesIfNeeded = false,
                excludeDefaults = false,
            )
            pushToServer(deviceId, password, file)
        }
    }

    private suspend fun pushToServer(deviceId: String, password: String, file: File) {
        withContext(Dispatchers.IO) {
            try {
                processRequest(
                    newRequestBuilder(deviceId, password)
                        .method(
                            "POST",
                            object : RequestBody() {
                                override fun contentType(): MediaType =
                                    "application/json".toMediaType()

                                override fun writeTo(sink: BufferedSink) {
                                    file.source().use {
                                        sink.writeAll(it)
                                    }
                                }

                                override fun contentLength(): Long =
                                    file.length()
                            },
                        )
                        .build(),
                )
            } finally {
                file.delete()
            }
        }
    }

    suspend fun download(deviceId: String, password: String): Importer.ImportStatus =
        withContext(Dispatchers.IO) {
            processRequest(
                newRequestBuilder(deviceId, password)
                    .build(),
            )
                .byteStream().use { inputStream ->
                    importer.importFromJSON(inputStream, importMode = ImportMode.REPLACE)
                }
        }

    private fun newRequestBuilder(deviceId: String, password: String) =
        Request.Builder()
            .url(baseUrl.toString())
            .addHeader(HttpHeaders.AUTHORIZATION, Credentials.basic(deviceId, password))
            .addHeader(HttpHeaders.USER_AGENT, UserAgentProvider.getUserAgent(context))

    private fun processRequest(request: Request): ResponseBody =
        client.newCall(request)
            .execute()
            .let {
                if (it.isSuccessful) {
                    it.body
                } else {
                    throw IOException()
                }
            }
}
