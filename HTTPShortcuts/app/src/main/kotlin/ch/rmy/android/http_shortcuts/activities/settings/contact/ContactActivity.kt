package ch.rmy.android.http_shortcuts.activities.settings.contact

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

class ContactActivity : BaseComposeActivity() {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var versionUtil: VersionUtil

    @Composable
    override fun ScreenScope.Content() {
        ContactScreen(
            onSubmit = {
                lifecycleScope.launch(Dispatchers.IO) {
                    createMailDraft()
                    lifecycleScope.launch {
                        finish()
                    }
                }
            }
        )
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    private fun createMailDraft() {
        sendMail(
            getString(R.string.developer_email_address),
            getString(R.string.email_subject_contact),
            getString(R.string.email_text_contact),
            getString(R.string.settings_mail),
            attachment = createMetaDataFile(),
        )
    }

    private fun sendMail(address: String, subject: String, text: String, title: String, attachment: Uri? = null) {
        try {
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
                .startActivity(this)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
        }
    }

    private fun createMetaDataFile(): Uri? =
        tryOrLog {
            FileUtil.createCacheFile(context, META_DATA_FILE)
                .also { uri ->
                    FileUtil.getWriter(context, uri).use {
                        GsonUtil.gson.toJson(collectMetaData(), it)
                    }
                }
        }

    private fun collectMetaData() =
        MetaData(
            androidSdkVersion = Build.VERSION.SDK_INT,
            appVersionCode = versionUtil.getVersionCode(),
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            language = Locale.getDefault().language,
            userId = settings.userId,
        )

    class IntentBuilder : BaseIntentBuilder(ContactActivity::class)

    companion object {
        private const val META_DATA_FILE = "app-details.json"
    }
}
