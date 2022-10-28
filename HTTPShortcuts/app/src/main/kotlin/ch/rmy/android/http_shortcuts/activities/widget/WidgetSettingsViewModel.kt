package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Application
import android.graphics.Color
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.ColorPickerFactory
import javax.inject.Inject

class WidgetSettingsViewModel(application: Application) :
    BaseViewModel<WidgetSettingsViewModel.InitData, WidgetSettingsViewState>(application),
    WithDialog {

    @Inject
    lateinit var colorPickerFactory: ColorPickerFactory

    init {
        getApplicationComponent().inject(this)
    }

    private val shortcutId: ShortcutId
        get() = initData.shortcutId
    private val shortcutName: String
        get() = initData.shortcutName
    private val shortcutIcon: ShortcutIcon
        get() = initData.shortcutIcon

    data class InitData(
        val shortcutId: ShortcutId,
        val shortcutName: String,
        val shortcutIcon: ShortcutIcon,
    )

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = WidgetSettingsViewState(
        showLabel = true,
        labelColor = Color.WHITE,
        shortcutIcon = shortcutIcon,
        shortcutName = shortcutName,
    )

    fun onLabelColorInputClicked() {
        showColorPicker()
    }

    private fun showColorPicker() {
        doWithViewState { viewState ->
            dialogState = createDialogState("widget-color-picker") {
                colorPickerFactory.createColorPicker(
                    onColorPicked = ::onLabelColorSelected,
                    onDismissed = {
                        dialogState?.let(::onDialogDismissed)
                    },
                    initialColor = viewState.labelColor,
                )
            }
        }
    }

    fun onShowLabelChanged(enabled: Boolean) {
        updateViewState {
            copy(showLabel = enabled)
        }
    }

    private fun onLabelColorSelected(color: Int) {
        updateViewState {
            copy(labelColor = color)
        }
    }

    fun onCreateButtonClicked() {
        doWithViewState { viewState ->
            finishWithOkResult(
                WidgetSettingsActivity.OpenWidgetSettings.createResult(
                    shortcutId = shortcutId,
                    labelColor = viewState.labelColorFormatted,
                    showLabel = viewState.showLabel,
                ),
            )
        }
    }
}
