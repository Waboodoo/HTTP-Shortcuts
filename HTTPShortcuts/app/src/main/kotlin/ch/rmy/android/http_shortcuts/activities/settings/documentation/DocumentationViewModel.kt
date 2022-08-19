package ch.rmy.android.http_shortcuts.activities.settings.documentation

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.utils.ExternalURLs

class DocumentationViewModel(application: Application) : BaseViewModel<DocumentationViewModel.InitData, Unit>(application) {

    private var currentUrl: Uri = Uri.EMPTY

    override fun initViewState() = Unit

    override fun onInitialized() {
        currentUrl = initData.url ?: ExternalURLs.DOCUMENTATION_PAGE.toUri()
        emitEvent(DocumentationEvent.LoadUrl(currentUrl))
    }

    fun onOpenInBrowserButtonClicked() {
        emitEvent(DocumentationEvent.OpenInBrowser(currentUrl))
    }

    fun onPageChanged(url: Uri) {
        currentUrl = url
    }

    data class InitData(
        val url: Uri?,
    )
}
