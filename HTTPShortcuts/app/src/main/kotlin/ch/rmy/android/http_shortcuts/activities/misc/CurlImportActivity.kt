package ch.rmy.android.http_shortcuts.activities.misc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.curlcommand.CurlParser
import kotterknife.bindView

class CurlImportActivity : BaseActivity() {

    private var commandEmpty = true
        set(value) {
            if (field != value) {
                field = value
                invalidateOptionsMenu()
            }
        }

    private val curlCommand: EditText by bindView(R.id.curl_import_command)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curl_import)

        curlCommand.observeTextChanges()
            .subscribe { text ->
                commandEmpty = text.isEmpty()
            }
            .attachTo(destroyer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.curl_import_activity_menu, menu)
        menu.findItem(R.id.action_create_from_curl).isVisible = !commandEmpty
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create_from_curl -> consume { startImport() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun startImport() {
        val commandString = curlCommand.text.toString()
        val command = CurlParser.parse(commandString)

        val intent = Intent()
            .putExtra(EXTRA_CURL_COMMAND, command)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override val navigateUpIcon = R.drawable.ic_clear

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, CurlImportActivity::class.java)

    companion object {

        const val EXTRA_CURL_COMMAND = "curl_command"

    }

}
