package ch.rmy.android.http_shortcuts.activities.contact

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class ContactViewModel
@Inject
constructor(
    application: Application,
    private val settings: Settings,
    private val versionUtil: VersionUtil,
    private val activityProvider: ActivityProvider,
) : BaseViewModel<Unit, Unit>(application) {
    override suspend fun initialize(data: Unit) = Unit
    fun onSubmit() = runAction {
        createMailDraft()
        closeScreen()
    }

    private suspend fun createMailDraft() {
        sendMail(
            context.getString(R.string.developer_email_address),
            context.getString(R.string.email_subject_contact),
            context.getString(R.string.email_text_contact),
            context.getString(R.string.settings_mail),
            attachment = createMetaDataFile(),
        )
    }

    private suspend fun sendMail(address: String, subject: String, text: String, title: String, attachment: Uri? = null) {
        try {
            activityProvider.withActivity { activity ->
                Intent(Intent.ACTION_SEND, "mailto:$address".toUri())
                    .setType("message/rfc822")
                    .putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                    .putExtra(Intent.EXTRA_SUBJECT, subject)
                    .putExtra(Intent.EXTRA_TEXT, text)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .runIfNotNull(attachment) {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .putExtra(Intent.EXTRA_STREAM, it)
                    }
                    .let {
                        Intent.createChooser(it, title)
                    }
                    .startActivity(activity)
            }
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
        }
    }

    private suspend fun createMetaDataFile(): Uri? =
        tryOrLog {
            withContext(Dispatchers.IO) {
                FileUtil.createCacheFile(context, META_DATA_FILE)
                    .also { uri ->
                        FileUtil.getWriter(context, uri).use {
                            GsonUtil.gson.toJson(collectMetaData(), it)
                        }
                    }
            }
        }

    private fun collectMetaData() =
        MetaData(
            androidSdkVersion = Build.VERSION.SDK_INT,
            appVersionCode = versionUtil.getVersionCode(),
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            language = Locale.getDefault().language,
            deviceId = settings.deviceId,
        )

    companion object {
        private const val META_DATA_FILE = "app-details.json"
    }
}
