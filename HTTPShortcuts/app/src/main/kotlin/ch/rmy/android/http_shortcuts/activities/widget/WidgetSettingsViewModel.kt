package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Color
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

class WidgetSettingsViewModel(application: Application) : BaseViewModel<WidgetSettingsViewModel.InitData, WidgetSettingsViewState>(application) {

    private val shortcutId: String
        get() = initData.shortcutId
    private val shortcutName: String
        get() = initData.shortcutName
    private val shortcutIcon: ShortcutIcon
        get() = initData.shortcutIcon

    data class InitData(
        val shortcutId: String,
        val shortcutName: String,
        val shortcutIcon: ShortcutIcon,
    )

    override fun initViewState() = WidgetSettingsViewState(
        showLabel = true,
        labelColor = Color.WHITE,
        shortcutIcon = shortcutIcon,
        shortcutName = shortcutName,
    )

    fun onLabelColorInputClicked() {
        emitEvent(WidgetSettingsEvent.ShowLabelColorPicker(currentViewState.labelColor))
    }

    fun onShowLabelChanged(enabled: Boolean) {
        updateViewState {
            copy(showLabel = enabled)
        }
    }

    fun onLabelColorSelected(color: Int) {
        updateViewState {
            copy(labelColor = color)
        }
    }

    fun onCreateButtonClicked() {
        finish(
            Activity.RESULT_OK,
            Intent()
                .putExtra(WidgetSettingsActivity.EXTRA_SHORTCUT_ID, shortcutId)
                .putExtra(WidgetSettingsActivity.EXTRA_SHOW_LABEL, currentViewState.showLabel)
                .putExtra(WidgetSettingsActivity.EXTRA_LABEL_COLOR, currentViewState.labelColorFormatted)
        )
    }
}
