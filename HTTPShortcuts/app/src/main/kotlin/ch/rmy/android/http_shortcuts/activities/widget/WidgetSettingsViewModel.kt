package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Application
import android.graphics.Color
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class WidgetSettingsViewModel(application: Application) :
    BaseViewModel<WidgetSettingsViewModel.InitData, WidgetSettingsViewState>(application),
    WithDialog {

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
            dialogState = DialogState.create("widget-color-picker") {
                ColorPickerDialog.Builder(context)
                    .setPositiveButton(
                        R.string.dialog_ok,
                        ColorEnvelopeListener { envelope, fromUser ->
                            if (fromUser) {
                                onLabelColorSelected(envelope.color)
                            }
                        },
                    )
                    .setNegativeButton(R.string.dialog_cancel) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .setOnDismissListener {
                        dialogState?.let(::onDialogDismissed)
                    }
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true)
                    .setBottomSpace(12)
                    .apply {
                        colorPickerView.setInitialColor(viewState.labelColor)
                    }
                    .create()
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
