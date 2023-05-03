package ch.rmy.android.http_shortcuts.activities.response

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.extensions.getSerializable
import ch.rmy.android.framework.extensions.toCharset
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.utils.ActivityCloser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DisplayResponseActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        DisplayResponseScreen(
            shortcutId = intent?.extras?.getString(EXTRA_SHORTCUT_ID) ?: "",
            shortcutName = intent?.extras?.getString(EXTRA_NAME) ?: "",
            text = intent?.extras?.getString(EXTRA_TEXT),
            mimeType = intent?.extras?.getString(EXTRA_TYPE),
            charset = intent?.getStringExtra(EXTRA_CHARSET)
                ?.toCharset()
                ?: Charsets.UTF_8,
            url = intent?.extras?.getString(EXTRA_URL)?.toUri(),
            fileUri = intent?.getParcelable(EXTRA_RESPONSE_FILE_URI),
            statusCode = intent?.extras?.getInt(EXTRA_STATUS_CODE)
                ?.takeUnless { it == 0 },
            headers = intent?.getSerializable(EXTRA_HEADERS) ?: emptyMap(),
            timing = intent?.extras?.getLong(EXTRA_TIMING)
                ?.takeUnless { it == 0L }
                ?.milliseconds,
            showDetails = intent?.extras?.getBoolean(EXTRA_DETAILS, false) ?: false,
            actions = (intent?.extras?.getStringArrayList(EXTRA_ACTIONS) ?: emptyList())
                .mapNotNull(ResponseDisplayAction::parse),
        )
    }

    private var autoFinishJob: Job? = null
    private var suppressAutoFinish = false

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is DisplayResponseEvent.SuppressAutoFinish -> suppressAutoFinish = true
            else -> super.handleEvent(event)
        }
    }

    override fun onStart() {
        super.onStart()
        suppressAutoFinish = false
        autoFinishJob?.cancel()
        autoFinishJob = null
    }

    override fun onStop() {
        super.onStop()
        if (!suppressAutoFinish) {
            autoFinishJob = lifecycleScope.launch {
                delay(FINISH_DELAY)
                finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCloser.onDisplayResponseActivityClosed()
        finish()
    }

    class IntentBuilder(shortcutId: ShortcutId) : BaseIntentBuilder(DisplayResponseActivity::class) {

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
            intent.putExtra(EXTRA_TEXT, text.truncate(MAX_TEXT_LENGTH))
        }

        fun responseFileUri(uri: Uri) = also {
            intent.putExtra(EXTRA_RESPONSE_FILE_URI, uri)
        }

        fun charset(charset: Charset) = also {
            intent.putExtra(EXTRA_CHARSET, charset.name())
        }

        fun url(url: String) = also {
            intent.putExtra(EXTRA_URL, url)
        }

        fun showDetails(showDetails: Boolean) = also {
            intent.putExtra(EXTRA_DETAILS, showDetails)
        }

        fun headers(headers: HttpHeaders?) = also {
            intent.putExtra(
                EXTRA_HEADERS,
                headers?.toMultiMap()?.let {
                    HashMap<String, ArrayList<String>>().apply {
                        it.forEach { (name, values) ->
                            put(name, ArrayList<String>().also { it.addAll(values) })
                        }
                    }
                }
            )
        }

        fun statusCode(statusCode: Int?) = also {
            intent.putExtra(EXTRA_STATUS_CODE, statusCode ?: return@also)
        }

        fun timing(timing: Long?) = also {
            intent.putExtra(EXTRA_TIMING, timing ?: return@also)
        }

        fun actions(actions: List<ResponseDisplayAction>) = also {
            intent.putStringArrayListExtra(EXTRA_ACTIONS, ArrayList(actions.map { it.key }))
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "id"
        private const val EXTRA_NAME = "name"
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_TEXT = "text"
        private const val EXTRA_CHARSET = "charset"
        private const val EXTRA_RESPONSE_FILE_URI = "response_file_uri"
        private const val EXTRA_URL = "url"
        private const val EXTRA_HEADERS = "headers"
        private const val EXTRA_STATUS_CODE = "status_code"
        private const val EXTRA_TIMING = "timing"
        private const val EXTRA_DETAILS = "details"
        private const val EXTRA_ACTIONS = "actions"

        private const val MAX_TEXT_LENGTH = 700000

        private val FINISH_DELAY = 8.seconds
    }
}
