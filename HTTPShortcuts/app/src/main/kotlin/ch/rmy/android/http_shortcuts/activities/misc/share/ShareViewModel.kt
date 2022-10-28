package ch.rmy.android.http_shortcuts.activities.misc.share

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.framework.viewmodel.viewstate.ProgressDialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import com.afollestad.materialdialogs.callbacks.onCancel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShareViewModel(application: Application) : BaseViewModel<ShareViewModel.InitData, ShareViewState>(application), WithDialog {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var shortcuts: List<ShortcutModel>
    private lateinit var variables: List<VariableModel>

    private val text: String
        get() = initData.text ?: ""
    private val title: String
        get() = initData.title ?: ""
    private lateinit var fileUris: List<Uri>

    private var currentJob: Job? = null

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = ShareViewState()

    override fun onInitializationStarted(data: InitData) {
        viewModelScope.launch {
            shortcuts = shortcutRepository.getShortcuts()
            variables = variableRepository.getVariables()
            finalizeInitialization()
        }
    }

    override fun onInitialized() {
        if (initData.fileUris.isEmpty()) {
            fileUris = emptyList()
            startShareFlow()
            return
        }

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            showProgressDialog(R.string.generic_processing_in_progress)
            try {
                fileUris = cacheSharedFiles(context, initData.fileUris)
                startShareFlow()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showToast(R.string.error_generic)
                logException(e)
                finish(skipAnimation = true)
            } finally {
                hideProgressDialog()
            }
        }
    }

    private fun startShareFlow() {
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

    private fun getTargetableShortcutsForFileSharing(isImage: Boolean?): List<ShortcutModel> =
        shortcuts
            .filter {
                it.hasFileParameter(isImage) ||
                    it.usesGenericFileBody() ||
                    (isImage != false && it.usesImageFileBody())
            }

    private fun handleFileSharing() {
        val isImage = fileUris.singleOrNull()
            ?.let(context.contentResolver::getType)
            ?.takeUnless { it == "application/octet-stream" }
            ?.let(FileTypeUtil::isImage)
        val shortcutsForFileSharing = getTargetableShortcutsForFileSharing(isImage)
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
        dialogState = createDialogState {
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
        dialogState = createDialogState {
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

    private fun showProgressDialog(message: Int) {
        dialogState = ProgressDialogState(StringResLocalizable(message), ::onProgressDialogCanceled)
    }

    private fun hideProgressDialog() {
        if (dialogState?.id == ProgressDialogState.DIALOG_ID) {
            dialogState = null
        }
    }

    private fun onProgressDialogCanceled() {
        currentJob?.cancel()
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

        private fun ShortcutModel.hasFileParameter(isImage: Boolean?): Boolean =
            parameters.any {
                when (it.parameterType) {
                    ParameterType.STRING -> false
                    ParameterType.FILE,
                    ParameterType.FILES,
                    -> true
                    ParameterType.IMAGE -> isImage != false
                }
            }

        @WorkerThread
        private fun cacheSharedFiles(context: Context, fileUris: List<Uri>): List<Uri> =
            fileUris
                .map { fileUri ->
                    context.contentResolver.openInputStream(fileUri)!!
                        .use { stream ->
                            FileUtil.createCacheFile(context, createCacheFileName())
                                .also { file ->
                                    FileUtil.getFileName(context.contentResolver, fileUri)
                                        ?.let { fileName ->
                                            FileUtil.putCacheFileOriginalName(file, fileName)
                                        }
                                    stream.copyTo(context.contentResolver.openOutputStream(file)!!)
                                }
                        }
                }

        private fun createCacheFileName() = "shared_${newUUID()}"
    }
}
