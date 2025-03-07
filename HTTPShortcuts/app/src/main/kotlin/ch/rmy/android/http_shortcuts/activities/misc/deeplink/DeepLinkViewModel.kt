package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeepLinkViewModel
@Inject
constructor(
    application: Application,
    private val shortcutRepository: ShortcutRepository,
    private val executionStarter: ExecutionStarter,
) : BaseViewModel<DeepLinkViewModel.InitData, DeepLinkViewState>(application) {
    override suspend fun initialize(data: InitData): DeepLinkViewState {
        val deepLinkUrl = initData.url
            ?: return DeepLinkViewState(
                dialogState = DeepLinkDialogState.Instructions,
            )

        if (deepLinkUrl.isCancelExecutions()) {
            sendIntent(
                MainActivity.IntentBuilder()
                    .cancelPendingExecutions(),
            )
            terminateInitialization()
        }

        val importUrl = deepLinkUrl.getImportUrl()
        if (importUrl != null) {
            sendIntent(
                MainActivity.IntentBuilder()
                    .importUrl(importUrl),
            )
            terminateInitialization()
        }

        val shortcutIdOrName = deepLinkUrl.getShortcutNameOrId()
        try {
            val shortcut = shortcutRepository.getShortcutByNameOrId(shortcutIdOrName)
            executeShortcut(shortcut.id, deepLinkUrl.getVariableValues())
            terminateInitialization()
        } catch (e: NoSuchElementException) {
            return DeepLinkViewState(
                dialogState = DeepLinkDialogState.ShortcutNotFound(shortcutIdOrName),
            )
        }
    }

    private fun executeShortcut(shortcutId: ShortcutId, variableValues: Map<VariableKey, String>) {
        executionStarter.execute(
            shortcutId = shortcutId,
            trigger = ShortcutTriggerType.DEEP_LINK,
            variableValues = variableValues,
        )
    }

    private fun Uri.isCancelExecutions() =
        host == "cancel-executions" && path?.trimEnd('/').isNullOrEmpty()

    private fun Uri.getImportUrl(): Uri? =
        takeIf {
            (host == "import" && path?.trimEnd('/').isNullOrEmpty()) || (scheme == "https" && path?.trim('/') == "import")
        }
            ?.getQueryParameter("url")
            ?.toUri()

    private fun Uri.getShortcutNameOrId(): ShortcutNameOrId =
        host
            ?.takeUnless { it == "deep-link" }
            ?: lastPathSegment
            ?: ""

    private fun Uri.getVariableValues(): Map<VariableKey, String> =
        queryParameterNames
            .filterNot { it.isEmpty() }
            .associateWith { key ->
                getQueryParameter(key) ?: ""
            }

    fun onDialogDismissed() = runAction {
        finish(skipAnimation = true)
    }

    data class InitData(
        val url: Uri?,
    )
}
