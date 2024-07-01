package ch.rmy.android.http_shortcuts.activities.workingdirectories

import android.net.Uri
import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class WorkingDirectoriesEvent : ViewModelEvent() {
    data class OpenDirectoryPicker(val initialDirectory: Uri? = null) : WorkingDirectoriesEvent()
}
