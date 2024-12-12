package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Application
import android.graphics.Color
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.widgets.WidgetsRepository
import ch.rmy.android.http_shortcuts.extensions.labelColorInt
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WidgetSettingsViewModel
@Inject
constructor(
    application: Application,
    private val widgetsRepository: WidgetsRepository,
) : BaseViewModel<WidgetSettingsViewModel.InitData, WidgetSettingsViewState>(application) {

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
        val widgetId: Int?,
    )

    override suspend fun initialize(data: InitData): WidgetSettingsViewState {
        val widget = data.widgetId
            ?.let {
                widgetsRepository.getWidgetById(it)
            }
        return WidgetSettingsViewState(
            showLabel = widget?.showLabel != false,
            showIcon = widget?.showIcon != false,
            labelColor = widget?.labelColorInt() ?: Color.WHITE,
            iconScale = widget?.iconScale ?: 1f,
            shortcutIcon = shortcutIcon,
            shortcutName = shortcutName,
        )
    }

    fun onLabelColorButtonClicked() = runAction {
        updateViewState {
            copy(colorDialogVisible = true)
        }
    }

    fun onShowLabelChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(showLabel = enabled)
        }
    }

    fun onShowIconChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(showIcon = enabled)
        }
    }

    fun onLabelColorSelected(color: Int) = runAction {
        updateViewState {
            copy(
                colorDialogVisible = false,
                labelColor = color,
            )
        }
    }

    fun onIconScaleChanged(scale: Float) = runAction {
        updateViewState {
            copy(iconScale = scale)
        }
    }

    fun onCreateButtonClicked() = runAction {
        closeScreen(
            result = NavigationDestination.Widget.Result(
                shortcutId = shortcutId,
                labelColor = viewState.labelColorFormatted,
                showLabel = viewState.showLabel,
                showIcon = viewState.showIcon,
                iconScale = viewState.iconScale,
            ),
        )
    }

    fun onDialogDismissalRequested() = runAction {
        updateViewState {
            copy(colorDialogVisible = false)
        }
    }
}
