package ch.rmy.android.http_shortcuts.activities.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.databinding.ActivityContactBinding
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import java.util.Locale
import javax.inject.Inject

class ContactActivity : BaseActivity() {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var versionUtil: VersionUtil

    private lateinit var binding: ActivityContactBinding

    private var inputValid = false
        set(value) {
            if (field != value) {
                field = value
                invalidateOptionsMenu()
            }
        }

    override fun inject(applicationComponent: ApplicationComponent) {
        getApplicationComponent().inject(this)
    }

    override fun onCreated(savedState: Bundle?) {
        binding = applyBinding(ActivityContactBinding.inflate(layoutInflater))
        setTitle(R.string.title_contact)

        binding.contactInstructions.text = getString(R.string.contact_instructions, CAPTCHA_CODE)

        binding.inputCaptcha.observeTextChanges()
            .subscribe {
                inputValid = binding.inputCaptcha.text.toString().equals(CAPTCHA_CODE, ignoreCase = true)
            }
            .attachTo(destroyer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.contact_activity_menu, menu)
        menu.findItem(R.id.action_create_contact_mail).isVisible = inputValid
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create_contact_mail -> consume {
            createMailDraft()
            finish()
        }
        else -> super.onOptionsItemSelected(item)
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
            userId = Settings(context).userId,
        )

    class IntentBuilder : BaseIntentBuilder(ContactActivity::class)

    companion object {
        private const val CAPTCHA_CODE = "HTTP Shortcuts"
        private const val META_DATA_FILE = "app-details.json"
    }
}
