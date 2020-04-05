package ch.rmy.android.http_shortcuts.activities

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.android.http_shortcuts.views.ResponseWebView
import ch.rmy.android.http_shortcuts.views.SyntaxHighlightView
import kotterknife.bindView

class DisplayResponseActivity : BaseActivity() {

    private val shortcutName: String by lazy {
        intent?.extras?.getString(EXTRA_NAME) ?: ""
    }
    private val text: String by lazy {
        intent?.extras?.getString(EXTRA_TEXT) ?: ""
    }
    private val type: String? by lazy {
        intent?.extras?.getString(EXTRA_TYPE)
    }
    private val url: String? by lazy {
        intent?.extras?.getString(EXTRA_URL)
    }

    private val responseText: TextView by bindView(R.id.response_text)
    private val formattedResponseText: SyntaxHighlightView by bindView(R.id.formatted_response_text)
    private val responseWebView: ResponseWebView by bindView(R.id.response_web_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = shortcutName
        updateViews()
    }

    private fun updateViews() {
        if (text.isBlank()) {
            displayAsPlainText(getString(R.string.message_blank_response), italic = true)
        } else {
            when (type) {
                TYPE_HTML -> {
                    displayInWebView(text, url)
                }
                TYPE_JSON -> {
                    displayWithSyntaxHighlighting(GsonUtil.prettyPrint(text), "json")
                }
                TYPE_XML -> {
                    displayWithSyntaxHighlighting(text, "xml")
                }
                TYPE_YAML, TYPE_YAML_ALT -> {
                    displayWithSyntaxHighlighting(text, "yaml")
                }
                else -> {
                    displayAsPlainText(text)
                }
            }
        }
    }

    private fun displayAsPlainText(text: String, italic: Boolean = false) {
        setContentView(R.layout.activity_display_response_plain)
        if (italic) {
            responseText.setTypeface(null, Typeface.ITALIC)
        }
        responseText.text = text
    }

    private fun displayInWebView(text: String, url: String?) {
        try {
            setContentView(R.layout.activity_display_response_webview)
            responseWebView.loadFromString(text, url)
        } catch (e: Exception) {
            logException(e)
            displayAsPlainText(text)
        }
    }

    private fun displayWithSyntaxHighlighting(text: String, language: String) {
        try {
            setContentView(R.layout.activity_display_response_syntax_highlighting)
            formattedResponseText.setCode(text, language)
        } catch (e: Exception) {
            logException(e)
            displayAsPlainText(text)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.display_response_activity_menu, menu)
        menu.findItem(R.id.action_share_response).isVisible = canShare()
        return super.onCreateOptionsMenu(menu)
    }

    private fun canShare() =
        text.length < MAX_SHARE_LENGTH

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_share_response -> consume { shareText() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun shareText() {
        if (!canShare()) {
            return
        }
        ShareUtil.shareText(context, text)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    class IntentBuilder(context: Context, shortcutId: String) : BaseIntentBuilder(context, DisplayResponseActivity::class.java) {

        init {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

        fun name(name: String) = also {
            intent.putExtra(EXTRA_NAME, name)
        }

        fun type(type: String?) = also {
            intent.putExtra(EXTRA_TYPE, type)
        }

        fun text(text: String) = also {
            intent.putExtra(EXTRA_TEXT, text)
        }

        fun url(url: String?) = also {
            intent.putExtra(EXTRA_URL, url)
        }

    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "id"
        private const val EXTRA_NAME = "name"
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_TEXT = "text"
        private const val EXTRA_URL = "url"

        private const val MAX_SHARE_LENGTH = 500000

        private const val TYPE_XML = "text/xml"
        private const val TYPE_JSON = "application/json"
        private const val TYPE_HTML = "text/html"
        private const val TYPE_YAML = "text/yaml"
        private const val TYPE_YAML_ALT = "application/x-yaml"
    }

}