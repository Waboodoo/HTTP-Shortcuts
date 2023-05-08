package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeepLinkViewModel(application: Application) : BaseViewModel<DeepLinkViewModel.InitData, DeepLinkViewState>(application) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = DeepLinkViewState()

    override fun onInitializationStarted(data: InitData) {
        finalizeInitialization(silent = true)
    }

    override fun onInitialized() {
        val deepLinkUrl = initData.url
        if (deepLinkUrl == null) {
            updateDialogState(DeepLinkDialogState.Instructions)
            return
        }

        if (deepLinkUrl.isCancelExecutions()) {
            openActivity(
                MainActivity.IntentBuilder()
                    .cancelPendingExecutions()
            )
            finish(skipAnimation = true)
            return
        }

        val importUrl = deepLinkUrl.getImportUrl()
        if (importUrl != null) {
            openActivity(
                MainActivity.IntentBuilder()
                    .importUrl(importUrl)
            )
            finish(skipAnimation = true)
            return
        }

        val shortcutIdOrName = deepLinkUrl.getShortcutNameOrId()
        viewModelScope.launch {
            try {
                val shortcut = shortcutRepository.getShortcutByNameOrId(shortcutIdOrName)
                executeShortcut(shortcut.id, deepLinkUrl.getVariableValues())
            } catch (e: NoSuchElementException) {
                updateDialogState(
                    DeepLinkDialogState.ShortcutNotFound(shortcutIdOrName)
                )
            }
        }
    }

    private fun executeShortcut(shortcutId: ShortcutId, variableValues: Map<VariableKey, String>) {
        openActivity(
            ExecuteActivity.IntentBuilder(shortcutId)
                .trigger(ShortcutTriggerType.DEEP_LINK)
                .variableValues(variableValues)
        )
        finish(skipAnimation = true)
    }

    private fun Uri.isCancelExecutions() =
        host == "cancel-executions" && path?.trimEnd('/').isNullOrEmpty()

    private fun Uri.getImportUrl(): Uri? =
        takeIf { host == "import" && path?.trimEnd('/').isNullOrEmpty() }
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

    fun onDialogDismissed() {
        finish(skipAnimation = true)
    }

    private fun updateDialogState(dialogState: DeepLinkDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(
        val url: Uri?,
    )
}
