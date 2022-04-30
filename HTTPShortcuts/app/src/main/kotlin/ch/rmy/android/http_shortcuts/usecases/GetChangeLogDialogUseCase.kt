package ch.rmy.android.http_shortcuts.usecases

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CheckBox
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import com.afollestad.materialdialogs.callbacks.onShow

class GetChangeLogDialogUseCase(
    private val settings: Settings,
) {

    @SuppressLint("SetJavaScriptEnabled")
    operator fun invoke(whatsNew: Boolean = false): DialogState =
        object : DialogState {

            private var stateRestored = false

            override fun createDialog(context: Context, viewModel: WithDialog): Dialog =
                DialogBuilder(context)
                    .view(R.layout.changelog_dialog)
                    .title(if (whatsNew) R.string.changelog_title_whats_new else R.string.changelog_title)
                    .positive(android.R.string.ok)
                    .build()
                    .onShow { dialog ->
                        settings.changeLogLastVersion = VersionUtil.getVersion(context)

                        val webView = dialog.findViewById<WebView>(R.id.changelog_webview)
                        val loadingIndicator = dialog.findViewById<View>(R.id.loading_indicator)
                        val showAtStartupCheckbox = dialog.findViewById<CheckBox>(R.id.checkbox_show_at_startup)

                        webView.settings.javaScriptEnabled = true
                        webView.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (context.isDarkThemeEnabled()) {
                                    webView.evaluateJavascript(
                                        """
                                        document.getElementById('root').className = 'dark';
                                    """
                                    ) {
                                        webView.isVisible = true
                                        loadingIndicator.isVisible = false
                                    }
                                } else {
                                    webView.isVisible = true
                                    loadingIndicator.isVisible = false
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView, url: String) = consume {
                                context.openURL(url)
                            }
                        }

                        if (!stateRestored) {
                            webView.loadUrl(CHANGELOG_ASSET_URL)
                        }

                        showAtStartupCheckbox.isChecked = !settings.isChangeLogPermanentlyHidden
                        showAtStartupCheckbox.setOnCheckedChangeListener { _, isChecked ->
                            settings.isChangeLogPermanentlyHidden = !isChecked
                        }
                    }

            override val id = DIALOG_ID

            override fun saveInstanceState(dialog: Dialog): Bundle =
                Bundle()
                    .also {
                        val webView = dialog.findViewById<WebView>(R.id.changelog_webview)
                        webView.saveState(it)
                    }

            override fun restoreInstanceState(dialog: Dialog, saveInstanceState: Bundle) {
                val webView = dialog.findViewById<WebView>(R.id.changelog_webview)
                webView.restoreState(saveInstanceState)
                stateRestored = true
            }
        }

    companion object {
        const val DIALOG_ID = "change-log"

        private const val CHANGELOG_ASSET_URL = "file:///android_asset/changelog.html"
    }
}
