package ch.rmy.android.http_shortcuts.activities.documentation

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.utils.ExternalURLs

class DocumentationViewModel(application: Application) : BaseViewModel<DocumentationViewModel.InitData, DocumentationViewState>(application) {

    override fun initViewState() = DocumentationViewState(
        initData.url ?: ExternalURLs.DOCUMENTATION_PAGE.toUri(),
    )

    fun onOpenInBrowserButtonClicked() {
        doWithViewState { viewState ->
            emitEvent(DocumentationEvent.OpenInBrowser(viewState.url))
        }
    }

    fun onPageChanged(url: Uri) {
        updateViewState {
            copy(url = url)
        }
    }

    fun onExternalUrl(url: Uri) {
        openURL(url)
    }

    data class InitData(
        val url: Uri?,
    )
}
