package ch.rmy.android.http_shortcuts.activities.misc.share

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import com.afollestad.materialdialogs.callbacks.onCancel

class ShareViewModel(application: Application) : BaseViewModel<ShareViewModel.InitData, ShareViewState>(application), WithDialog {

    private val shortcutRepository = ShortcutRepository()
    private val variableRepository = VariableRepository()

    private lateinit var shortcuts: List<ShortcutModel>
    private lateinit var variables: List<VariableModel>

    private val text: String
        get() = initData.text ?: ""
    private val title: String
        get() = initData.title ?: ""
    private val fileUris: List<Uri>
        get() = initData.fileUris

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = ShareViewState()

    override fun onInitializationStarted(data: InitData) {
        shortcutRepository.getShortcuts()
            .subscribe { shortcuts ->
                this.shortcuts = shortcuts
                variableRepository.getVariables()
                    .subscribe { variables ->
                        this.variables = variables
                        finalizeInitialization()
                    }
                    .attachTo(destroyer)
            }
            .attachTo(destroyer)
    }

    override fun onInitialized() {
        if (text.isEmpty()) {
            handleFileSharing()
        } else {
            handleTextSharing()
        }
    }

    private fun handleTextSharing() {
        val variableLookup = VariableManager(variables)
        val variables = getTargetableVariablesForTextSharing()
        val variableIds = variables.map { it.id }.toSet()
        val shortcuts = getTargetableShortcutsForTextSharing(variableIds, variableLookup)

        val variableValues = variables.associate { variable ->
            variable.key to when {
                variable.isShareText && variable.isShareTitle -> "$title - $text"
                variable.isShareTitle -> title
                else -> text
            }
        }
        when (shortcuts.size) {
            0 -> showInstructions(R.string.error_not_suitable_shortcuts)
            1 -> {
                executeShortcut(shortcuts[0].id, variableValues = variableValues)
            }
            else -> {
                showShortcutSelection(shortcuts, variableValues = variableValues)
            }
        }
    }

    private fun getTargetableVariablesForTextSharing() =
        variables
            .filter { it.isShareText || it.isShareTitle }
            .toSet()

    private fun getTargetableShortcutsForTextSharing(variableIds: Set<VariableId>, variableLookup: VariableLookup): List<ShortcutModel> =
        shortcuts
            .filter { it.hasShareVariable(variableIds, variableLookup) }

    private fun getTargetableShortcutsForFileSharing(): List<ShortcutModel> =
        shortcuts
            .filter { it.hasFileParameter() || it.usesFileBody() }

    private fun handleFileSharing() {
        val shortcutsForFileSharing = getTargetableShortcutsForFileSharing()
        when (shortcutsForFileSharing.size) {
            0 -> {
                showInstructions(R.string.error_not_suitable_shortcuts)
            }
            1 -> {
                executeShortcut(shortcutsForFileSharing[0].id)
            }
            else -> showShortcutSelection(shortcutsForFileSharing)
        }
    }

    private fun executeShortcut(shortcutId: ShortcutId, variableValues: Map<VariableKey, String> = emptyMap()) {
        openActivity(
            ExecuteActivity.IntentBuilder(shortcutId)
                .variableValues(variableValues)
                .files(fileUris)
        )
        finish(skipAnimation = true)
    }

    private fun showInstructions(@StringRes text: Int) {
        dialogState = DialogState.create {
            message(text)
                .positive(R.string.dialog_ok) {
                    onInstructionsCanceled()
                }
                .build()
                .onCancel {
                    onInstructionsCanceled()
                }
        }
    }

    private fun onInstructionsCanceled() {
        finish(skipAnimation = true)
    }

    private fun showShortcutSelection(shortcuts: List<ShortcutModel>, variableValues: Map<VariableKey, String> = emptyMap()) {
        dialogState = DialogState.create {
            runFor(shortcuts) { shortcut ->
                item(name = shortcut.name, shortcutIcon = shortcut.icon) {
                    executeShortcut(shortcut.id, variableValues)
                }
            }
                .build()
                .onCancel {
                    onShortcutSelectionCanceled()
                }
        }
    }

    private fun onShortcutSelectionCanceled() {
        finish(skipAnimation = true)
    }

    data class InitData(
        val text: String?,
        val title: String?,
        val fileUris: List<Uri>,
    )

    companion object {

        private fun ShortcutModel.hasShareVariable(variableIds: Set<VariableId>, variableLookup: VariableLookup): Boolean {
            val variableIdsInShortcut = VariableResolver.extractVariableIds(this, variableLookup)
            return variableIds.any { variableIdsInShortcut.contains(it) }
        }

        private fun ShortcutModel.hasFileParameter(): Boolean =
            parameters.any { it.isFileParameter || it.isFilesParameter }
    }
}
