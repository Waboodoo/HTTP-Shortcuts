package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.http.HttpClients
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.Settings
import io.reactivex.Completable
import io.reactivex.Single

class RemoteEditViewModel(application: Application) : AndroidViewModel(application) {

    private val remoteBaseUrl = Uri.parse(REMOTE_BASE_URL)
        .buildUpon()
        .appendEncodedPath(REMOTE_API_PATH)
        .build()

    private val settings = Settings(context)
    private val remoteEditManager by lazy {
        RemoteEditManager(
            context = context,
            client = HttpClients.getClient(),
            baseUrl = remoteBaseUrl,
            exporter = Exporter(context),
            importer = Importer(context),
        )
    }

    val deviceId: String
        get() = settings.remoteEditDeviceId
            ?: run {
                generateDeviceId()
                    .also {
                        settings.remoteEditDeviceId = it
                    }
            }

    var password: String
        get() = settings.remoteEditPassword ?: ""
        set(value) {
            settings.remoteEditPassword = value
        }

    private fun generateDeviceId(): String =
        (0 until DEVICE_ID_LENGTH).map {
            DEVICE_ID_CHARACTERS.random()
        }
            .joinToString(separator = "")

    val editorAddress: String
        get() = REMOTE_BASE_URL.replace("https://", "")

    fun upload(): Completable =
        remoteEditManager.upload(deviceId, password)

    fun download(): Single<Importer.ImportStatus> =
        remoteEditManager.download(deviceId, password)

    companion object {

        private const val REMOTE_BASE_URL = "https://http-shortcuts.rmy.ch/editor"
        private const val REMOTE_API_PATH = "api/files/"

        private const val DEVICE_ID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789"
        private const val DEVICE_ID_LENGTH = 8

    }

}