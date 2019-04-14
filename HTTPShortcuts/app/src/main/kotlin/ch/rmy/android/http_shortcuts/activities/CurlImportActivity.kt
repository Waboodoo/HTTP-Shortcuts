package ch.rmy.android.http_shortcuts.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
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

        curlCommand.observeTextChanges()
            .subscribe { text ->
                commandEmpty = text.isEmpty()
            }
            .attachTo(destroyer)
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

        ShortcutEditorActivity.IntentBuilder(context)
            .curlCommand(command)
            .build()
            .startActivity(this, REQUEST_CREATE_SHORTCUT)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CREATE_SHORTCUT && intent != null) {
            val shortcutId = intent.getStringExtra(ShortcutEditorActivity.RESULT_SHORTCUT_ID)
            setResult(RESULT_OK, Intent().putExtra(ShortcutEditorActivity.RESULT_SHORTCUT_ID, shortcutId))
        }
        finish()
    }

    override val navigateUpIcon = R.drawable.ic_clear

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, CurlImportActivity::class.java)

    companion object {

        private const val REQUEST_CREATE_SHORTCUT = 1

    }

}
