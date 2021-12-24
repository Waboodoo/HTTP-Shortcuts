package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.http_shortcuts.http.HttpClients
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.Settings
import io.reactivex.Completable
import io.reactivex.Single

class RemoteEditViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = Settings(context)

    var serverUrl: String
        get() = settings.remoteEditServerUrl ?: REMOTE_BASE_URL
        set(value) {
            settings.remoteEditServerUrl = value
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

    val humanReadableEditorAddress: String
        get() = getRemoteBaseUrl().toString().replace("https://", "")

    fun upload(): Completable =
        getRemoteEditManager().upload(deviceId, password)

    fun download(): Single<Importer.ImportStatus> =
        getRemoteEditManager().download(deviceId, password)

    private fun getRemoteBaseUrl() =
        serverUrl.toUri()

    private fun getRemoteEditManager() =
        RemoteEditManager(
            context = context,
            client = HttpClients.getClient(context),
            baseUrl = getRemoteBaseUrl()
                .buildUpon()
                .appendEncodedPath(REMOTE_API_PATH)
                .build(),
            exporter = Exporter(context),
            importer = Importer(context),
        )

    companion object {

        private const val REMOTE_BASE_URL = "https://http-shortcuts.rmy.ch/editor"
        private const val REMOTE_API_PATH = "api/files/"

        private const val DEVICE_ID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789"
        private const val DEVICE_ID_LENGTH = 8
    }
}
