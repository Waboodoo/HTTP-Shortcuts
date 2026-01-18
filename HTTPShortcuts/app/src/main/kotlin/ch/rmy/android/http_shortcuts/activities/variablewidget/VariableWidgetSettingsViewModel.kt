package ch.rmy.android.http_shortcuts.activities.variablewidget

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.variablewidget.models.SelectableShortcut
import ch.rmy.android.http_shortcuts.activities.variablewidget.models.SelectableVariable
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.widgets.VariableWidgetsRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VariableWidgetSettingsViewModel
@Inject
constructor(
    application: Application,
    private val globalVariableRepository: GlobalVariableRepository,
    private val shortcutRepository: ShortcutRepository,
    private val variableWidgetsRepository: VariableWidgetsRepository,
) : BaseViewModel<VariableWidgetSettingsViewModel.InitData, VariableWidgetSettingsViewState>(application) {

    private lateinit var constants: List<GlobalVariable>

    override suspend fun initialize(data: InitData): VariableWidgetSettingsViewState {
        val widget = data.widgetId
            ?.let {
                variableWidgetsRepository.getVariableWidgetById(it)
            }

        constants = globalVariableRepository.getGlobalVariables()
            .filter { it.type == VariableType.CONSTANT }
        val selectableVariables = constants.map { it.toSelectableVariable() }
        val selectedVariable = widget
            ?.let { constants.find { it.id == widget.variableId } }
            ?: constants.firstOrNull()

        val selectableShortcuts = shortcutRepository.getShortcuts()
            .map { shortcut ->
                SelectableShortcut(shortcutId = shortcut.id, name = shortcut.name)
            }

        return VariableWidgetSettingsViewState(
            selectableVariables = selectableVariables,
            selectedVariable = selectedVariable?.toSelectableVariable(),
            variableValue = selectedVariable?.value,
            fontSize = widget?.fontSize ?: 22,
            title = widget?.title ?: "",
            shortcutId = widget?.shortcutId,
            selectableShortcuts = selectableShortcuts,
        )
    }

    private fun GlobalVariable.toSelectableVariable() =
        SelectableVariable(
            variableId = id,
            variableKey = key,
        )

    fun onVariableSelected(variableId: GlobalVariableId?) = runAction {
        updateViewState {
            copy(
                selectedVariable = selectableVariables.find { it.variableId == variableId },
                variableValue = constants.find { it.id == variableId }?.value,
            )
        }
    }

    fun onFontSizeChanged(fontSize: Int) = runAction {
        updateViewState {
            copy(
                fontSize = fontSize,
            )
        }
    }

    fun onTitleChanged(title: String) = runAction {
        updateViewState {
            copy(
                title = title,
            )
        }
    }

    fun onShortcutSelected(shortcutId: ShortcutId?) = runAction {
        updateViewState {
            copy(
                shortcutId = shortcutId,
            )
        }
    }

    fun onSubmitButtonClicked() = runAction {
        val variableId = viewState.selectedVariable?.variableId ?: skipAction()
        closeScreen(
            result = NavigationDestination.VariableWidget.Result(
                variableId = variableId,
                fontSize = viewState.fontSize,
                title = viewState.title,
                shortcutId = viewState.shortcutId,
            ),
        )
    }

    fun onBackPressed() = runAction {
        closeScreen(
            result = NavigationDestination.VariableWidget.RESULT_VARIABLE_WIDGET_SETTINGS_CANCELLED,
        )
    }

    data class InitData(
        val widgetId: Int?,
    )
}
