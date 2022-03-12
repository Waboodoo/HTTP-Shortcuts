package ch.rmy.android.http_shortcuts.activities.icons

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class IconPickerEvent : ViewModelEvent() {
    object ShowImagePicker : IconPickerEvent()
}
