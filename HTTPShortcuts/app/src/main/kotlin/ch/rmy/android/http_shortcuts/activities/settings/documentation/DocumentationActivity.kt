package ch.rmy.android.http_shortcuts.activities.settings.documentation

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.extensions.tryOrIgnore
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityDocumentationBinding
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil

class DocumentationActivity : BaseActivity() {

    private lateinit var binding: ActivityDocumentationBinding

    private val viewModel: DocumentationViewModel by bindViewModel()

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(
            DocumentationViewModel.InitData(
                url = intent.getParcelable(EXTRA_URL),
            )
        )
        initViews(savedState)
        initViewModelBindings()
    }

    private fun initViews(savedState: Bundle?) {
        binding = applyBinding(ActivityDocumentationBinding.inflate(layoutInflater))
        setTitle(R.string.title_documentation)
        binding.webView.initWebView()
        savedState?.let(binding.webView::restoreState)
    }

    private fun WebView.initWebView() {
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.isForMainFrame) {
                    val externalUrl = DocumentationUrlManager.toExternal(request.url)
                    val internalUrl = DocumentationUrlManager.toInternalUrl(externalUrl)
                    if (internalUrl != null) {
                        loadUrl(internalUrl.toString())
                    } else {
                        openURL(externalUrl)
                    }
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (!request.isForMainFrame && request.url.path.equals("/favicon.ico")) {
                    return tryOrIgnore {
                        WebResourceResponse("image/png", null, null)
                    }
                }
                return null
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showLoading()
            }

            override fun onPageFinished(view: WebView, url: String) {
                viewModel.onPageChanged(DocumentationUrlManager.toExternal(url.toUri()))
                if (context.isDarkThemeEnabled()) {
                    evaluateJavascript("document.getElementById('root').className = 'dark';") {
                        hideLoading()
                    }
                } else {
                    hideLoading()
                }
                evaluateJavascript("""document.getElementsByTagName("h1")[0].innerText""") { pageTitle ->
                    setSubtitle(pageTitle.trim('"').takeUnless { it.isEmpty() || it == "null" || it == "Documentation" }?.toLocalizable())
                }
            }
        }

        with(settings) {
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            javaScriptEnabled = true
            allowContentAccess = false
            allowFileAccess = false
            userAgentString = UserAgentUtil.userAgent
        }
    }

    private fun showLoading() {
        binding.webView.isVisible = false
        binding.loadingIndicator.isVisible = true
    }

    private fun hideLoading() {
        binding.webView.postDelayed(50) {
            binding.webView.isVisible = true
            binding.loadingIndicator.isVisible = false
        }
    }

    private fun initViewModelBindings() {
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is DocumentationEvent.LoadUrl -> {
                DocumentationUrlManager.toInternalUrl(event.url)
                    ?.let { url ->
                        binding.webView.loadUrl(url.toString())
                    }
            }
            is DocumentationEvent.OpenInBrowser -> {
                openURL(event.url)
            }
            else -> super.handleEvent(event)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.documentation_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_open_in_browser -> consume {
            viewModel.onOpenInBrowserButtonClicked()
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    class IntentBuilder : BaseIntentBuilder(DocumentationActivity::class) {

        fun url(url: Uri) = also {
            intent.putExtra(EXTRA_URL, url)
        }
    }

    companion object {
        private const val EXTRA_URL = "url"
    }
}
