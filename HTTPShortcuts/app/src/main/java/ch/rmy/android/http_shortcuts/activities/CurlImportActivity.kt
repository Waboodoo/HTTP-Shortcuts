package ch.rmy.android.http_shortcuts.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.consume
import ch.rmy.android.http_shortcuts.utils.onTextChanged
import ch.rmy.curlcommand.CurlParser
import kotterknife.bindView
import kotlin.properties.Delegates

class CurlImportActivity : BaseActivity() {

    private var commandEmpty by Delegates.observable(true) { _, old, new ->
        if (old != new) {
            invalidateOptionsMenu()
        }
    }

    private val curlCommand: EditText by bindView(R.id.curl_import_command)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curl_import)

        curlCommand.onTextChanged { text ->
            commandEmpty = text.isEmpty()
        }.attachTo(destroyer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.curl_import_activity_menu, menu)
        menu.findItem(R.id.action_create_from_curl).isVisible = curlCommand.text.isNotEmpty()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create_from_curl -> consume { startImport() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun startImport() {
        val commandString = curlCommand.text.toString()
        val command = CurlParser.parse(commandString)

        val intent = EditorActivity.IntentBuilder(context)
                .curlCommand(command)
                .build()
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CREATE_SHORTCUT && intent != null) {
            val shortcutId = intent.getLongExtra(EditorActivity.EXTRA_SHORTCUT_ID, 0)
            val returnIntent = Intent()
            returnIntent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
            setResult(Activity.RESULT_OK, returnIntent)
        }
        finish()
    }

    override val navigateUpIcon = R.drawable.ic_clear

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, CurlImportActivity::class.java)

    companion object {

        private const val REQUEST_CREATE_SHORTCUT = 1

        const val EXTRA_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.activities.CurlImportActivity.shortcut_id"

    }

}
