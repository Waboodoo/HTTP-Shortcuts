package ch.rmy.android.http_shortcuts.activities.misc

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityAcknowledgmentsBinding

class AcknowledgmentActivity : BaseActivity() {

    private lateinit var binding: ActivityAcknowledgmentsBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreated(savedState: Bundle?) {
        tryOrLog {
            binding = applyBinding(ActivityAcknowledgmentsBinding.inflate(layoutInflater))
            setTitle(R.string.title_licenses)
            with(binding.acknowledgmentsWebview) {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        if (context.isDarkThemeEnabled()) {
                            evaluateJavascript("document.getElementById('root').className = 'dark';") {
                                reveal()
                            }
                        } else {
                            reveal()
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String) = consume {
                        context.openURL(url)
                    }
                }

                settings.javaScriptEnabled = true
                if (savedState != null) {
                    restoreState(savedState)
                } else {
                    loadUrl(ACKNOWLEDGMENTS_ASSET_URL)
                }
            }
        }
            ?: run {
                showToast(R.string.error_generic)
                finish()
            }
    }

    private fun reveal() {
        binding.acknowledgmentsWebview.isVisible = true
        binding.loadingIndicator.isVisible = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.acknowledgmentsWebview.saveState(outState)
    }

    class IntentBuilder : BaseIntentBuilder(AcknowledgmentActivity::class.java)

    companion object {

        private const val ACKNOWLEDGMENTS_ASSET_URL = "file:///android_asset/acknowledgments.html"
    }
}
