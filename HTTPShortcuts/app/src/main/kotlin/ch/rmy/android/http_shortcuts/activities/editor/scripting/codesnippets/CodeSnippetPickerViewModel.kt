package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import android.app.Application
import android.net.Uri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases.GenerateCodeSnippetItemsUseCase
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases.GetItemWrappersUseCase
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.actions.types.PlaySoundActionType
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.usecases.GetBuiltInIconPickerDialogUseCase
import ch.rmy.android.http_shortcuts.usecases.GetIconPickerDialogUseCase
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.ExternalURLs.getScriptingDocumentation
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CodeSnippetPickerViewModel(application: Application) :
    BaseViewModel<CodeSnippetPickerViewModel.InitData, CodeSnippetPickerViewState>(application), WithDialog {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var getIconPickerDialog: GetIconPickerDialogUseCase

    @Inject
    lateinit var getBuiltInIconPickerDialog: GetBuiltInIconPickerDialogUseCase

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

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    private var sectionItems: List<SectionItem> = emptyList()
    private var expandedSections = mutableSetOf<Int>()

    override fun onInitializationStarted(data: InitData) {
        Single.fromCallable {
            generateCodeSnippetItems(initData, ::onCodeSnippetItemEvent)
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { sectionItems ->
                this.sectionItems = sectionItems
                finalizeInitialization()
            }
            .attachTo(destroyer)
    }

    override fun initViewState() = CodeSnippetPickerViewState(
        items = computeItemWrappers(),
    )

    private fun computeItemWrappers(query: String? = null): List<ItemWrapper> =
        getItemWrappers(sectionItems, expandedSections, query)

    override fun onInitialized() {
        variableRepository.getObservableVariables()
            .subscribe(variablePlaceholderProvider::applyVariables)
            .attachTo(destroyer)
        shortcutRepository.getObservableShortcuts()
            .subscribe(shortcutPlaceholderProvider::applyShortcuts)
            .attachTo(destroyer)
    }

    private fun onCodeSnippetItemEvent(event: GenerateCodeSnippetItemsUseCase.Event) {
        when (event) {
            is GenerateCodeSnippetItemsUseCase.Event.InsertText -> {
                returnResult(event.textBeforeCursor, event.textAfterCursor)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickIcon -> {
                openIconPicker(event.shortcutPlaceholder)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickNotificationSound -> {
                emitEvent(CodeSnippetPickerEvent.OpenRingtonePicker)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickShortcut -> {
                showShortcutPicker(event.title, event.andThen)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickTaskerTask -> {
                emitEvent(CodeSnippetPickerEvent.OpenTaskerTaskPicker)
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickVariableForReading -> {
                onPickVariableForReading()
            }
            is GenerateCodeSnippetItemsUseCase.Event.PickVariableForWriting -> {
                onPickVariableForWriting()
            }
        }
    }

    private fun showShortcutPicker(title: Int, callback: (shortcutPlaceholder: String) -> Unit) {
        val currentShortcutId = initData.currentShortcutId
        if (shortcutPlaceholderProvider.placeholders.none { it.id != currentShortcutId }) {
            callback("\"\"")
            return
        }
        dialogState = DialogState.create {
            title(title)
                .item(R.string.label_insert_action_code_for_current_shortcut) {
                    callback("\"\"")
                }
                .runFor(shortcutPlaceholderProvider.placeholders) { shortcut ->
                    runIf(shortcut.id != currentShortcutId) {
                        item(name = shortcut.name, shortcutIcon = shortcut.icon) {
                            callback("/*[shortcut]*/\"${shortcut.id}\"/*[/shortcut]*/")
                        }
                    }
                }
                .build()
        }
    }

    private fun openIconPicker(shortcutPlaceholder: String) {
        iconPickerShortcutPlaceholder = shortcutPlaceholder
        dialogState = getIconPickerDialog(
            callbacks = object : GetIconPickerDialogUseCase.Callbacks {
                override fun openBuiltInIconSelectionDialog() {
                    dialogState = getBuiltInIconPickerDialog(::onIconSelected)
                }

                override fun openCustomIconPicker() {
                    emitEvent(CodeSnippetPickerEvent.OpenCustomIconPicker)
                }

                override fun openIpackPicker() {
                    emitEvent(CodeSnippetPickerEvent.OpenIpackIconPicker)
                }
            },
        )
    }

    private fun onPickVariableForReading() {
        if (!variablePlaceholderProvider.hasVariables) {
            showGetVariablesInstructionDialog()
            return
        }
        dialogState = DialogState.create {
            runFor(variablePlaceholderProvider.placeholders) { variable ->
                item(name = variable.variableKey, descriptionRes = VariableTypeMappings.getTypeName(variable.variableType)) {
                    returnResult("getVariable(/*[variable]*/\"${variable.variableId}\"/*[/variable]*/)", "")
                }
            }
                .build()
        }
    }

    private fun showGetVariablesInstructionDialog() {
        dialogState = DialogState.create {
            title(R.string.help_title_variables)
                .message(R.string.help_text_code_snippet_get_variable_no_variable)
                .negative(android.R.string.cancel)
                .positive(R.string.button_create_first_variable) { openVariableEditor() }
                .build()
        }
    }

    private fun openVariableEditor() {
        openActivity(
            VariablesActivity.IntentBuilder()
        )
    }

    private fun onPickVariableForWriting() {
        if (!variablePlaceholderProvider.hasVariables) {
            showSetVariablesInstructionDialog()
            return
        }
        dialogState = DialogState.create {
            runFor(variablePlaceholderProvider.placeholders) { variable ->
                item(name = variable.variableKey, descriptionRes = VariableTypeMappings.getTypeName(variable.variableType)) {
                    returnResult("setVariable(/*[variable]*/\"${variable.variableId}\"/*[/variable]*/, \"", "\");\n")
                }
            }
                .build()
        }
    }

    private fun showSetVariablesInstructionDialog() {
        dialogState = DialogState.create {
            title(R.string.help_title_variables)
                .message(R.string.help_text_code_snippet_set_variable_no_variable)
                .negative(android.R.string.cancel)
                .positive(R.string.button_create_first_variable) { openVariableEditor() }
                .build()
        }
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

    fun onSectionClicked(id: Int) {
        doWithViewState { viewState ->
            if (!viewState.searchQuery.isNullOrBlank()) {
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

    fun onCodeSnippetClicked(id: Int) {
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

    fun onCodeSnippetDocRefButtonClicked(id: Int) {
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

    fun onSearchSubmitted(query: String) {
        updateSearchQuery(query)
    }

    fun onSearchTyped(query: String) {
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

    fun onBackPressed() {
        doWithViewState { viewState ->
            if (viewState.searchQuery.isNotEmpty()) {
                updateSearchQuery("")
                emitEvent(CodeSnippetPickerEvent.UpdateSearch)
            } else {
                finish()
            }
        }
    }

    data class InitData(
        val currentShortcutId: ShortcutId?,
        val includeResponseOptions: Boolean,
        val includeNetworkErrorOption: Boolean,
        val includeFileOptions: Boolean,
    )

    companion object {
        private fun escape(input: String) =
            input.replace("\"", "\\\"")
    }
}
