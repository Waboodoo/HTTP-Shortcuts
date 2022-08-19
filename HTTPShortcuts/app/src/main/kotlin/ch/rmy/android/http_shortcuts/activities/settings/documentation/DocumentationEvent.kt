package ch.rmy.android.http_shortcuts.activities.settings.documentation

import android.net.Uri
import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class DocumentationEvent : ViewModelEvent() {
    data class LoadUrl(val url: Uri) : DocumentationEvent()
    data class OpenInBrowser(val url: Uri) : DocumentationEvent()
}
