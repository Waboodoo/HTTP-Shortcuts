package ch.rmy.android.http_shortcuts.activities.documentation

import android.net.Uri
import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class DocumentationEvent : ViewModelEvent() {
    data class OpenInBrowser(val url: Uri) : DocumentationEvent()
}
