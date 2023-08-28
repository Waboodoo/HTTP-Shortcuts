package ch.rmy.android.http_shortcuts.activities.misc.share

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareViewModel(application: Application) : BaseViewModel<ShareViewModel.InitData, ShareViewState>(application) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var shortcuts: List<Shortcut>
    private lateinit var variables: List<Variable>

    private val text: String
        get() = initData.text ?: ""
    private val title: String
        get() = initData.title ?: ""
    private lateinit var fileUris: List<Uri>

    private lateinit var shortcutsForFileSharing: List<Shortcut>
    private var variableValues: Map<VariableKey, String> = emptyMap()

    override suspend fun initialize(data: InitData): ShareViewState {
        shortcuts = shortcutRepository.getShortcuts()
        variables = variableRepository.getVariables()

        if (initData.fileUris.isEmpty()) {
            fileUris = emptyList()
            viewModelScope.launch {
                startShareFlow()
            }
        } else {
            viewModelScope.launch {
                try {
                    fileUris = withContext(Dispatchers.IO) {
                        cacheSharedFiles(context, initData.fileUris)
                    }
                    startShareFlow()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    showToast(R.string.error_generic)
                    logException(e)
                    finish(skipAnimation = true)
                }
            }
        }
        return ShareViewState(
            dialogState = ShareDialogState.Progress,
        )
    }

    private suspend fun startShareFlow() {
        if (text.isEmpty()) {
            handleFileSharing()
        } else {
            handleTextSharing()
        }
    }

    private suspend fun handleTextSharing() {
        val variableLookup = VariableManager(variables)
        val variables = getTargetableVariablesForTextSharing()
        val variableIds = variables.map { it.id }.toSet()
        val shortcuts = getTargetableShortcutsForTextSharing(variableIds, variableLookup)

        variableValues = variables.associate { variable ->
            variable.key to when {
                variable.isShareText && variable.isShareTitle -> "$title - $text"
                variable.isShareTitle -> title
                else -> text
            }
        }
        when (shortcuts.size) {
            0 -> updateDialogState(ShareDialogState.Instructions)
            1 -> executeShortcut(shortcuts[0].id, variableValues = variableValues)
            else -> showShortcutSelection(shortcuts)
        }
    }

    private fun getTargetableVariablesForTextSharing() =
        variables
            .filter { it.isShareText || it.isShareTitle }
            .toSet()

    private fun getTargetableShortcutsForTextSharing(variableIds: Set<VariableId>, variableLookup: VariableLookup): List<Shortcut> =
        shortcuts
            .filter { it.hasShareVariable(variableIds, variableLookup) }

    private fun getTargetableShortcutsForFileSharing(isImage: Boolean?): List<Shortcut> =
        shortcuts
            .filter {
                it.hasFileParameter(isImage) ||
                    it.usesGenericFileBody() ||
                    (isImage != false && it.fileUploadOptions?.type == FileUploadType.CAMERA)
            }

    private suspend fun handleFileSharing() {
        val isImage = fileUris.singleOrNull()
            ?.let(context.contentResolver::getType)
            ?.takeUnless { it == "application/octet-stream" }
            ?.let(FileTypeUtil::isImage)
        shortcutsForFileSharing = getTargetableShortcutsForFileSharing(isImage)
        when (shortcutsForFileSharing.size) {
            0 -> updateDialogState(ShareDialogState.Instructions)
            1 -> executeShortcut(shortcutsForFileSharing[0].id)
            else -> showShortcutSelection(shortcutsForFileSharing)
        }
    }

    private suspend fun executeShortcut(shortcutId: ShortcutId, variableValues: Map<VariableKey, String> = emptyMap()) {
        openActivity(
            ExecuteActivity.IntentBuilder(shortcutId)
                .variableValues(variableValues)
                .files(fileUris)
                .trigger(ShortcutTriggerType.SHARE)
        )
        finish(skipAnimation = true)
    }

    private suspend fun showShortcutSelection(shortcuts: List<Shortcut>) {
        updateDialogState(
            ShareDialogState.PickShortcut(
                shortcuts.map { it.toShortcutPlaceholder() },
            )
        )
    }

    fun onShortcutSelected(shortcutId: ShortcutId) = runAction {
        executeShortcut(shortcutId, variableValues)
    }

    fun onDialogDismissed() = runAction {
        finish(skipAnimation = true)
    }

    private suspend fun updateDialogState(dialogState: ShareDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(
        val text: String?,
        val title: String?,
        val fileUris: List<Uri>,
    )

    companion object {

        internal fun Shortcut.hasShareVariable(variableIds: Set<VariableId>, variableLookup: VariableLookup): Boolean {
            val variableIdsInShortcut = VariableResolver.extractVariableIdsIncludingScripting(this, variableLookup)
            return variableIds.any { variableIdsInShortcut.contains(it) }
        }

        internal fun Shortcut.hasFileParameter(isImage: Boolean? = null): Boolean =
            parameters.any {
                when (it.parameterType) {
                    ParameterType.STRING -> false
                    ParameterType.FILE -> {
                        it.fileUploadOptions?.type != FileUploadType.CAMERA || isImage != false
                    }
                }
            }

        @WorkerThread
        internal fun cacheSharedFiles(context: Context, fileUris: List<Uri>): List<Uri> =
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
                                    context.contentResolver.getType(fileUri)
                                        ?.let { fileType ->
                                            FileUtil.putCacheFileOriginalType(file, fileType)
                                        }
                                    stream.copyTo(context.contentResolver.openOutputStream(file)!!)
                                }
                        }
                }

        private fun createCacheFileName() = "shared_${newUUID()}"
    }
}
