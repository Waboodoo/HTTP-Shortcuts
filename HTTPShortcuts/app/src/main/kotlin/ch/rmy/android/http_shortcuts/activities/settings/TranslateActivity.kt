package ch.rmy.android.http_shortcuts.activities.settings

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.sendMail
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import kotterknife.bindView

class TranslateActivity : BaseActivity() {

    private val languageInput: EditText by bindView(R.id.input_language)
    private val githubProfileInput: EditText by bindView(R.id.input_github_profile)

    private var inputValid = false
        set(value) {
            if (field != value) {
                field = value
                invalidateOptionsMenu()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        languageInput.observeTextChanges()
            .subscribe {
                inputValid = languageInput.text.isNotEmpty()
            }
            .attachTo(destroyer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.translate_activity_menu, menu)
        menu.findItem(R.id.action_create_translate_mail).isVisible = inputValid
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create_translate_mail -> consume {
            createMailDraft()
            finish()
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun createMailDraft() {
        sendMail(
            getString(R.string.developer_email_address),
            getString(R.string.email_subject_translate),
            getEmailText(),
            getString(R.string.settings_help_translate)
        )
    }

    private fun getEmailText(): String =
        getString(R.string.email_text_translate, languageInput.text.toString())
            .mapIf(githubProfileInput.text.isNotEmpty()) {
                it.plus(
                    getString(R.string.email_text_translate_github_optional, githubProfileInput.text.toString())
                )
            }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, TranslateActivity::class.java)

}