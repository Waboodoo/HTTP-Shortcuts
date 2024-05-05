package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models.ItemWrapper
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models.SectionItem
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases.GenerateCodeSnippetItemsUseCase
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases.GetItemWrappersUseCase
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scripting.actions.types.PlaySoundActionType
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.ExternalURLs.getScriptingDocumentation
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CodeSnippetPickerViewModel
@Inject
constructor(
    application: Application,
    private val shortcutRepository: ShortcutRepository,
    private val variableRepository: VariableRepository,
    private val generateCodeSnippetItems: GenerateCodeSnippetItemsUseCase,
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
    private val shortcutPlaceholderProvider: ShortcutPlaceholderProvider,
    private val getItemWrappers: GetItemWrappersUseCase,
) :
    BaseViewModel<CodeSnippetPickerViewModel.InitData, CodeSnippetPickerViewState>(application) {

    private var iconPickerShortcutPlaceholder: String? = null
    private var onShortcutSelected: ((String) -> Unit)? = null

    private var sectionItems: List<SectionItem> = emptyList()
    private var expandedSections = mutableSetOf<String>()

    override suspend fun initialize(data: InitData): CodeSnippetPickerViewState {
        sectionItems = withContext(Dispatchers.Default) {
            generateCodeSnippetItems(initData) { event ->
                runAction {
                    onCodeSnippetItemEvent(event)
                }
            }
        }
        viewModelScope.launch {
            variableRepository.getObservableVariables()
                .collect(variablePlaceholderProvider::applyVariables)
        }
        viewModelScope.launch {
            shortcutRepository.getObservableShortcuts()
                .collect(shortcutPlaceholderProvider::applyShortcuts)
        }
        return CodeSnippetPickerViewState(
            items = computeItemWrappers(),
        )
    }

    private fun computeItemWrappers(query: String? = null): List<ItemWrapper> =
        getItemWrappers(sectionItems, expandedSections, query)

    private suspend fun onCodeSnippetItemEvent(event: GenerateCodeSnippetItemsUseCase.Event) {
        when (event) {
            is GenerateCodeSnippetItemsUseCase.Event.InsertText -> {
                returnResult(event.textBeforeCursor, event.textAfterCursor)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickIcon -> {
                iconPickerShortcutPlaceholder = event.shortcutPlaceholder
                updateDialogState(CodeSnippetPickerDialogState.SelectIcon)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickNotificationSound -> {
                emitEvent(CodeSnippetPickerEvent.OpenRingtonePicker)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickShortcut -> {
                onShortcutSelected = event.andThen
                showShortcutPicker(event.title, event.andThen)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickTaskerTask -> {
                emitEvent(CodeSnippetPickerEvent.OpenTaskerTaskPicker)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickVariableForReading -> {
                updateDialogState(CodeSnippetPickerDialogState.SelectVariableForReading(variablePlaceholderProvider.placeholders))
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickVariableForWriting -> {
                updateDialogState(CodeSnippetPickerDialogState.SelectVariableForWriting(variablePlaceholderProvider.placeholders))
            }
        }
    }

    private suspend fun showShortcutPicker(title: Int, callback: (shortcutPlaceholder: String) -> Unit) {
        val currentShortcutId = initData.currentShortcutId
        val placeholders = shortcutPlaceholderProvider.placeholders
        if (placeholders.none { it.id != currentShortcutId }) {
            callback("\"\"")
            return
        }
        onShortcutSelected = callback

        updateDialogState(
            CodeSnippetPickerDialogState.SelectShortcut(
                title = StringResLocalizable(title),
                shortcuts = placeholders,
            )
        )
    }

    fun onVariableEditorButtonClicked() = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.Variables)
    }

    fun onVariableSelected(variableId: VariableId) = runAction {
        val variableKey = variablePlaceholderProvider.findPlaceholderById(variableId)
            ?.variableKey
            ?: skipAction()
        when (viewState.dialogState) {
            is CodeSnippetPickerDialogState.SelectVariableForReading -> {
                updateDialogState(null)
                returnResult("getVariable(\"${variableKey}\")", "")
            }
            is CodeSnippetPickerDialogState.SelectVariableForWriting -> {
                updateDialogState(null)
                returnResult("setVariable(\"${variableKey}\", \"", "\");\n")
            }
            else -> skipAction()
        }
    }

    fun onIconSelected(icon: ShortcutIcon) = runAction {
        val shortcutPlaceholder = iconPickerShortcutPlaceholder ?: skipAction()
        returnResult("changeIcon($shortcutPlaceholder, \"$icon\");\n", "")
    }

    fun onRingtoneSelected(ringtone: Uri) = runAction {
        val soundDescriptor = ringtone.toString()
            .removePrefix(PlaySoundActionType.CONTENT_PREFIX)
        returnResult("playSound(\"${escape(soundDescriptor)}\");", "")
    }

    fun onTaskerTaskSelected(taskName: String) = runAction {
        returnResult("triggerTaskerTask(\"${escape(taskName)}\");", "")
    }

    private suspend fun returnResult(textBeforeCursor: String, textAfterCursor: String) {
        closeScreen(result = NavigationDestination.CodeSnippetPicker.Result(textBeforeCursor, textAfterCursor))
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    fun onSectionClicked(id: String) = runAction {
        if (viewState.searchQuery.isNotBlank()) {
            skipAction()
        }
        if (expandedSections.contains(id)) {
            expandedSections.remove(id)
        } else {
            expandedSections.add(id)
        }
        updateViewState {
            copy(
                items = computeItemWrappers(),
            )
        }
    }

    fun onCodeSnippetClicked(id: String) = runAction {
        (
            viewState.items
                .firstOrNull { it is ItemWrapper.CodeSnippet && it.id == id }
                as? ItemWrapper.CodeSnippet
            )
            ?.codeSnippetItem
            ?.action
            ?.invoke()
    }

    fun onCodeSnippetDocRefButtonClicked(id: String) = runAction {
        (
            viewState.items
                .firstOrNull { it is ItemWrapper.CodeSnippet && it.id == id }
                as? ItemWrapper.CodeSnippet
            )
            ?.codeSnippetItem
            ?.docRef
            ?.let(::getScriptingDocumentation)
            ?.let { openURL(it) }
    }

    fun onSearchQueryChanged(query: String) = runAction {
        updateSearchQuery(query)
    }

    private suspend fun updateSearchQuery(query: String) {
        updateViewState {
            copy(
                searchQuery = query,
                items = computeItemWrappers(query),
            )
        }
    }

    fun onShortcutSelected(shortcutId: ShortcutId) = runAction {
        val callback = onShortcutSelected ?: skipAction()
        onShortcutSelected = null
        val shortcutName = shortcutPlaceholderProvider.findPlaceholderById(shortcutId)
            ?.name
            ?: skipAction()
        updateDialogState(null)
        callback("\"${shortcutName}\"")
    }

    fun onCurrentShortcutSelected() = runAction {
        val callback = onShortcutSelected ?: skipAction()
        onShortcutSelected = null
        updateDialogState(null)
        callback("\"\"")
    }

    fun onDialogDismissRequested() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: CodeSnippetPickerDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onCustomIconOptionSelected() = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.IconPicker)
    }

    data class InitData(
        val currentShortcutId: ShortcutId?,
        val includeSuccessOptions: Boolean,
        val includeResponseOptions: Boolean,
        val includeNetworkErrorOption: Boolean,
    )

    companion object {
        internal fun escape(input: String) =
            input.replace("\"", "\\\"")
    }
}
