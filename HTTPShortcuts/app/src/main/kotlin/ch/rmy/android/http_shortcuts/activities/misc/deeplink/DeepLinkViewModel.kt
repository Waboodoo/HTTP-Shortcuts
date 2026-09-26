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
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeepLinkViewModel
@Inject
constructor(
    application: Application,
    private val shortcutRepository: ShortcutRepository,
    private val executionStarter: ExecutionStarter,
    private val globalVariableRepository: GlobalVariableRepository,
    private val shareUtil: ShareUtil,
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
            executeShortcut(
                shortcutId = shortcut.id,
                variableValues = determineVariableValues(deepLinkUrl, initData.title, initData.text),
            )
            terminateInitialization()
        } catch (_: NoSuchElementException) {
            return DeepLinkViewState(
                dialogState = DeepLinkDialogState.ShortcutNotFound(shortcutIdOrName),
            )
        }
    }

    private suspend fun determineVariableValues(deepLinkUrl: Uri, title: String?, text: String?): Map<VariableKeyOrId, String> = buildMap {
        if (title != null || text != null) {
            val variables = globalVariableRepository.getGlobalVariables()
            val globalVariables = shareUtil.getTextShareGlobalVariables(variables)

            globalVariables.forEach { variable ->
                when {
                    variable.isShareText && variable.isShareTitle && title != null && text != null -> "$title - $text"
                    variable.isShareTitle && title != null -> title
                    text != null -> text
                    else -> null
                }
                    ?.let {
                        put(VariableKeyOrId(variable.key), it)
                    }
            }
        }

        deepLinkUrl.queryParameterNames
            .filterNot { it.isEmpty() }
            .forEach { key ->
                put(VariableKeyOrId(key), (deepLinkUrl.getQueryParameter(key) ?: ""))
            }
    }

    private fun executeShortcut(shortcutId: ShortcutId, variableValues: Map<VariableKeyOrId, String>) {
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

    fun onDialogDismissed() = runAction {
        finish(skipAnimation = true)
    }

    data class InitData(
        val url: Uri?,
        val title: String?,
        val text: String?,
    )
}
