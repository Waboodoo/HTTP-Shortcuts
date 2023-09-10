package ch.rmy.android.http_shortcuts.activities.misc.share

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.data.RealmUnavailableException
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShareViewModel
@Inject
constructor(
    application: Application,
    private val shortcutRepository: ShortcutRepository,
    private val variableRepository: VariableRepository,
    private val shareUtil: ShareUtil,
) : BaseViewModel<ShareViewModel.InitData, ShareViewState>(application) {
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
        try {
            shortcuts = shortcutRepository.getShortcuts()
                .runIfNotNull(data.shortcutId) { shortcutId ->
                    filter { it.id == shortcutId }
                        .takeUnlessEmpty()
                        ?: this
                }
            variables = variableRepository.getVariables()
        } catch (e: RealmUnavailableException) {
            showToast(R.string.error_generic, long = true)
            terminateInitialization()
        }

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
        val variables = shareUtil.getTextShareVariables(variables)
        val variableIds = variables.map { it.id }.toSet()
        val shortcuts = getTextShareTargets(variableIds, variableLookup)

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

    private fun getTextShareTargets(variableIds: Set<VariableId>, variableLookup: VariableLookup): List<Shortcut> =
        shortcuts
            .filter { shareUtil.isTextShareTarget(it, variableIds, variableLookup) }

    private fun getTargetableShortcutsForFileSharing(isImage: Boolean?): List<Shortcut> =
        shortcuts
            .filter { shareUtil.isFileShareTarget(it, isImage) }

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
        sendIntent(
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
        val shortcutId: ShortcutId?,
    )

    companion object {
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
