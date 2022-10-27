package ch.rmy.android.http_shortcuts.activities.variables.editor.types.select

import android.app.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.variables.types.SelectType

class SelectTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, SelectTypeViewState>(application) {

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = SelectTypeViewState(
        options = computeOptionList(),
        isMultiSelect = SelectType.isMultiSelect(variable),
        separator = SelectType.getSeparator(variable),
    )

    private fun computeOptionList() =
        variable.options?.map { OptionItem(it.id, it.labelOrValue) } ?: emptyList()

    override fun onVariableChanged() {
        updateViewState {
            copy(options = computeOptionList())
        }
    }

    fun onOptionClicked(id: String) {
        val option = variable.options?.firstOrNull { it.id == id } ?: return
        emitEvent(
            SelectTypeEvent.ShowEditDialog(
                optionId = id,
                label = option.label,
                value = option.value,
            )
        )
    }

    fun onOptionMoved(optionId1: String, optionId2: String) {
        launchWithProgressTracking {
            temporaryVariableRepository.moveOption(optionId1, optionId2)
        }
    }

    override fun validate() =
        if (variable.options.isNullOrEmpty()) {
            showSnackbar(R.string.error_not_enough_select_values)
            false
        } else true

    fun onAddButtonClicked() {
        emitEvent(SelectTypeEvent.ShowAddDialog)
    }

    fun onMultiSelectChanged(enabled: Boolean) {
        updateViewState {
            copy(isMultiSelect = enabled)
        }
        doWithViewState { viewState ->
            saveData(viewState)
        }
    }

    fun onSeparatorChanged(separator: String) {
        updateViewState {
            copy(separator = separator)
        }
        doWithViewState { viewState ->
            saveData(viewState)
        }
    }

    fun onAddDialogConfirmed(label: String, value: String) {
        launchWithProgressTracking {
            temporaryVariableRepository.addOption(label, value)
        }
    }

    fun onEditDialogConfirmed(optionId: String, label: String, value: String) {
        launchWithProgressTracking {
            temporaryVariableRepository.updateOption(optionId, label, value)
        }
    }

    fun onDeleteOptionSelected(optionId: String) {
        launchWithProgressTracking {
            temporaryVariableRepository.removeOption(optionId)
        }
    }

    private fun saveData(viewState: SelectTypeViewState) {
        launchWithProgressTracking {
            temporaryVariableRepository.setDataForType(
                mapOf(
                    SelectType.KEY_MULTI_SELECT to viewState.isMultiSelect.toString(),
                    SelectType.KEY_SEPARATOR to viewState.separator,
                )
            )
        }
    }
}
