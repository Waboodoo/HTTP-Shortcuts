package ch.rmy.android.http_shortcuts.activities.icons

import android.net.Uri
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.activities.icons.models.IconShape

abstract class IconPickerEvent : ViewModelEvent() {
    object ShowImagePicker : IconPickerEvent()
    data class ShowImageCropper(val imageUri: Uri, val shape: IconShape) : IconPickerEvent()
}
