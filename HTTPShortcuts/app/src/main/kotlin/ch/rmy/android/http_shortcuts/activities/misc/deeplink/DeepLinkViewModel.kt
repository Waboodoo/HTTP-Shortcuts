package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.app.Application
import android.net.Uri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import com.afollestad.materialdialogs.callbacks.onCancel

class DeepLinkViewModel(application: Application) : BaseViewModel<DeepLinkViewModel.InitData, DeepLinkViewState>(application), WithDialog {

    private val shortcutRepository = ShortcutRepository()

    override fun initViewState() = DeepLinkViewState()

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: InitData) {
        finalizeInitialization(silent = true)
    }

    private fun showMessageDialog(message: Localizable) {
        dialogState = DialogState.create {
            message(message)
                .positive(R.string.dialog_ok) {
                    onMessageDialogCanceled()
                }
                .build()
                .onCancel {
                    onMessageDialogCanceled()
                }
        }
    }

    private fun onMessageDialogCanceled() {
        finish(skipAnimation = true)
    }

    override fun onInitialized() {
        val deepLinkUrl = initData.url
        if (deepLinkUrl == null) {
            showMessageDialog(
                Localizable.create { context ->
                    HTMLUtil.format(context.getString(R.string.instructions_deep_linking, EXAMPLE_URL))
                }
            )
            return
        }

        val shortcutIdOrName = deepLinkUrl.getShortcutNameOrId()

        shortcutRepository.getShortcutByNameOrId(shortcutIdOrName)
            .subscribe(
                { shortcut ->
                    executeShortcut(shortcut.id, deepLinkUrl.getVariableValues())
                },
                {
                    showMessageDialog(StringResLocalizable(R.string.error_shortcut_not_found_for_deep_link, shortcutIdOrName))
                },
            )
            .attachTo(destroyer)
    }

    private fun executeShortcut(shortcutId: ShortcutId, variableValues: Map<VariableKey, String>) {
        openActivity(
            ExecuteActivity.IntentBuilder(shortcutId)
                .variableValues(variableValues)
        )
        finish(skipAnimation = true)
    }

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

    data class InitData(
        val url: Uri?,
    )

    companion object {
        private const val EXAMPLE_URL = "http-shortcuts://<b>&lt;Name/ID of Shortcut&gt;</b>"
    }
}
