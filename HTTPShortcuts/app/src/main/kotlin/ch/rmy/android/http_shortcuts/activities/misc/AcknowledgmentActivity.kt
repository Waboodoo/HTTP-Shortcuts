package ch.rmy.android.http_shortcuts.activities.misc

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.doOnDestroy
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
                                revealDelayed()
                            }
                        } else {
                            revealDelayed()
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = consume {
                        context.openURL(request.url)
                    }
                }

                settings.javaScriptEnabled = true
                if (savedState != null) {
                    restoreState(savedState)
                } else {
                    loadUrl(ACKNOWLEDGMENTS_ASSET_URL)
                }
                doOnDestroy { destroy() }
            }
        }
            ?: run {
                showToast(R.string.error_generic)
                finish()
            }
    }

    internal fun revealDelayed() {
        binding.acknowledgmentsWebview.postDelayed(50) {
            binding.acknowledgmentsWebview.isVisible = true
            binding.loadingIndicator.isVisible = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.acknowledgmentsWebview.saveState(outState)
    }

    class IntentBuilder : BaseIntentBuilder(AcknowledgmentActivity::class)

    companion object {

        private const val ACKNOWLEDGMENTS_ASSET_URL = "file:///android_asset/acknowledgments.html"
    }
}
