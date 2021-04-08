package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.FileUtil
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.Okio
import java.io.File
import java.io.IOException

class RemoteEditManager(
    private val context: Context,
    private val client: OkHttpClient,
    private val baseUrl: Uri,
    private val exporter: Exporter,
    private val importer: Importer,
) {

    fun upload(deviceId: String, password: String): Completable =
        Single.just(File(context.cacheDir, "remote-edit.json"))
            .flatMapCompletable { file ->
                exporter.exportToUri(FileUtil.getUriFromFile(context, file), format = ExportFormat.LEGACY_JSON)
                    .flatMapCompletable {
                        pushToServer(deviceId, password, file)
                    }
            }
            .subscribeOn(Schedulers.io())

    private fun pushToServer(deviceId: String, password: String, file: File) =
        Completable.fromAction {
            try {
                processRequest(
                    newRequestBuilder(deviceId, password)
                        .method("POST", object : RequestBody() {
                            override fun contentType(): MediaType = MediaType.get("application/json")

                            override fun writeTo(sink: BufferedSink) {
                                Okio.source(file).use {
                                    sink.writeAll(it)
                                }
                            }

                            override fun contentLength(): Long = file.length()
                        })
                        .build()
                )
            } finally {
                file.delete()
            }
        }

    fun download(deviceId: String, password: String): Single<Importer.ImportStatus> =
        Single.fromCallable {
            processRequest(
                newRequestBuilder(deviceId, password)
                    .build()
            )
                .byteStream().use { inputStream ->
                    importer.importFromJSON(inputStream, importMode = Importer.ImportMode.REPLACE)
                }
        }
            .subscribeOn(Schedulers.io())

    private fun newRequestBuilder(deviceId: String, password: String) =
        Request.Builder()
            .url(baseUrl.toString())
            .addHeader(HttpHeaders.AUTHORIZATION, Credentials.basic(deviceId, password))

    private fun processRequest(request: Request): ResponseBody =
        client.newCall(request)
            .execute()
            .let {
                if (it.isSuccessful) {
                    it.body()!!
                } else {
                    throw IOException()
                }
            }

}