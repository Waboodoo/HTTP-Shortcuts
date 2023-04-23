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
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.actions.types.PlaySoundActionType
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.ExternalURLs.getScriptingDocumentation
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CodeSnippetPickerViewModel(application: Application) :
    BaseViewModel<CodeSnippetPickerViewModel.InitData, CodeSnippetPickerViewState>(application) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var generateCodeSnippetItems: GenerateCodeSnippetItemsUseCase

    @Inject
    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    @Inject
    lateinit var shortcutPlaceholderProvider: ShortcutPlaceholderProvider

    @Inject
    lateinit var getItemWrappers: GetItemWrappersUseCase

    init {
        getApplicationComponent().inject(this)
    }

    private var iconPickerShortcutPlaceholder: String? = null
    private var onShortcutSelected: ((String) -> Unit)? = null

    private var sectionItems: List<SectionItem> = emptyList()
    private var expandedSections = mutableSetOf<String>()

    override fun onInitializationStarted(data: InitData) {
        viewModelScope.launch {
            this@CodeSnippetPickerViewModel.sectionItems = withContext(Dispatchers.Default) {
                generateCodeSnippetItems(initData, ::onCodeSnippetItemEvent)
            }
            finalizeInitialization()
        }
    }

    override fun initViewState() = CodeSnippetPickerViewState(
        items = computeItemWrappers(),
    )

    private fun computeItemWrappers(query: String? = null): List<ItemWrapper> =
        getItemWrappers(sectionItems, expandedSections, query)

    override fun onInitialized() {
        viewModelScope.launch {
            variableRepository.getObservableVariables()
                .collect(variablePlaceholderProvider::applyVariables)
        }
        viewModelScope.launch {
            shortcutRepository.getObservableShortcuts()
                .collect(shortcutPlaceholderProvider::applyShortcuts)
        }
    }

    private fun onCodeSnippetItemEvent(event: GenerateCodeSnippetItemsUseCase.Event) {
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

    private fun showShortcutPicker(title: Int, callback: (shortcutPlaceholder: String) -> Unit) {
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

    fun onVariableEditorButtonClicked() {
        updateDialogState(null)
        openActivity(
            VariablesActivity.IntentBuilder()
        )
    }

    fun onVariableSelected(variableId: VariableId) {
        val variableKey = variablePlaceholderProvider.findPlaceholderById(variableId)
            ?.variableKey
            ?: return
        when (currentViewState?.dialogState) {
            is CodeSnippetPickerDialogState.SelectVariableForReading -> {
                returnResult("getVariable(\"${variableKey}\")", "")
            }
            is CodeSnippetPickerDialogState.SelectVariableForWriting -> {
                returnResult("setVariable(\"${variableKey}\", \"", "\");\n")
            }
            else -> return
        }
        updateDialogState(null)
    }

    fun onIconSelected(icon: ShortcutIcon) {
        val shortcutPlaceholder = iconPickerShortcutPlaceholder ?: return
        returnResult("changeIcon($shortcutPlaceholder, \"$icon\");\n", "")
    }

    fun onRingtoneSelected(ringtone: Uri) {
        val soundDescriptor = ringtone.toString()
            .removePrefix(PlaySoundActionType.CONTENT_PREFIX)
        returnResult("playSound(\"${escape(soundDescriptor)}\");", "")
    }

    fun onTaskerTaskSelected(taskName: String) {
        returnResult("triggerTaskerTask(\"${escape(taskName)}\");", "")
    }

    private fun returnResult(textBeforeCursor: String, textAfterCursor: String) {
        finishWithOkResult(
            CodeSnippetPickerActivity.PickCodeSnippet.createResult(textBeforeCursor, textAfterCursor)
        )
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    fun onSectionClicked(id: String) {
        doWithViewState { viewState ->
            if (viewState.searchQuery.isNotBlank()) {
                return@doWithViewState
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
    }

    fun onCodeSnippetClicked(id: String) {
        doWithViewState { viewState ->
            (
                viewState.items
                    .firstOrNull { it is ItemWrapper.CodeSnippet && it.id == id }
                    as? ItemWrapper.CodeSnippet
                )
                ?.codeSnippetItem
                ?.action
                ?.invoke()
        }
    }

    fun onCodeSnippetDocRefButtonClicked(id: String) {
        doWithViewState { viewState ->
            (
                viewState.items
                    .firstOrNull { it is ItemWrapper.CodeSnippet && it.id == id }
                    as? ItemWrapper.CodeSnippet
                )
                ?.codeSnippetItem
                ?.docRef
                ?.let(::getScriptingDocumentation)
                ?.let(::openURL)
        }
    }

    fun onSearchQueryChanged(query: String) {
        updateSearchQuery(query)
    }

    private fun updateSearchQuery(query: String) {
        updateViewState {
            copy(
                searchQuery = query,
                items = computeItemWrappers(query),
            )
        }
    }

    fun onShortcutSelected(shortcutId: ShortcutId) {
        val callback = onShortcutSelected ?: return
        onShortcutSelected = null
        val shortcutName = shortcutPlaceholderProvider.findPlaceholderById(shortcutId)
            ?.name
            ?: return
        callback("\"${shortcutName}\"")
    }

    fun onCurrentShortcutSelected() {
        val callback = onShortcutSelected ?: return
        onShortcutSelected = null
        callback("\"\"")
    }

    fun onDialogDismissRequested() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: CodeSnippetPickerDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(
        val currentShortcutId: ShortcutId?,
        val includeResponseOptions: Boolean,
        val includeNetworkErrorOption: Boolean,
    )

    companion object {
        internal fun escape(input: String) =
            input.replace("\"", "\\\"")
    }
}
