package ch.rmy.android.http_shortcuts.activities.settings


import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.sendMail
import ch.rmy.android.http_shortcuts.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import kotterknife.bindView
import java.util.Locale

class ContactActivity : BaseActivity() {

    private val instructions: TextView by bindView(R.id.contact_instructions)
    private val captchaInput: EditText by bindView(R.id.input_captcha)

    private var inputValid = false
        set(value) {
            if (field != value) {
                field = value
                invalidateOptionsMenu()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        setTitle(R.string.title_contact)

        instructions.text = getString(R.string.contact_instructions, CAPTCHA_CODE)

        captchaInput.observeTextChanges()
            .subscribe {
                inputValid = captchaInput.text.toString().equals(CAPTCHA_CODE, ignoreCase = true)
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
            attachment = createMetaDataFile()
        )
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
            appVersion = packageManager.getPackageInfo(packageName, 0).versionName,
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            language = Locale.getDefault().language,
            userId = Settings(context).userId
        )

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ContactActivity::class.java)

    companion object {
        private const val CAPTCHA_CODE = "HTTP Shortcuts"
        private const val META_DATA_FILE = "app-details.json"
    }

}