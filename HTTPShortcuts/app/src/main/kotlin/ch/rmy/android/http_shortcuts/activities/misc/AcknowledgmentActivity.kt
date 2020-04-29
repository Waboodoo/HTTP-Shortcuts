package ch.rmy.android.http_shortcuts.activities.misc

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import kotterknife.bindView

class AcknowledgmentActivity : BaseActivity() {

    private val webView: WebView by bindView(R.id.acknowledgments_webview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tryOrLog {
            setContentView(R.layout.activity_acknowledgments)
            webView.loadUrl(ACKNOWLEDGMENTS_ASSET_URL)
        } ?: run {
            showToast(R.string.error_generic)
            finish()
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, AcknowledgmentActivity::class.java)

    companion object {

        private const val ACKNOWLEDGMENTS_ASSET_URL = "file:///android_asset/acknowledgments.html"

    }

}
