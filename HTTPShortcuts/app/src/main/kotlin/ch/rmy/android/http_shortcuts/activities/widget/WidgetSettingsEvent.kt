package ch.rmy.android.http_shortcuts.activities.widget

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class WidgetSettingsEvent : ViewModelEvent() {
    data class ShowLabelColorPicker(val initialColor: Int) : WidgetSettingsEvent()
}
