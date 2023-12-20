package ch.rmy.android.http_shortcuts.activities.documentation

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DocumentationViewModel
@Inject
constructor(
    application: Application,
) : BaseViewModel<DocumentationViewModel.InitData, DocumentationViewState>(application) {

    override suspend fun initialize(data: InitData) = DocumentationViewState(
        initData.url ?: ExternalURLs.DOCUMENTATION_PAGE.toUri(),
    )

    fun onOpenInBrowserButtonClicked() = runAction {
        emitEvent(DocumentationEvent.OpenInBrowser(viewState.url))
    }

    fun onPageChanged(url: Uri) = runAction {
        updateViewState {
            copy(url = url)
        }
    }

    fun onExternalUrl(url: Uri) = runAction {
        if (url.toString() == ExternalURLs.CONTACT_PAGE) {
            navigate(NavigationDestination.Contact)
        } else {
            openURL(url)
        }
    }

    data class InitData(
        val url: Uri?,
    )
}
