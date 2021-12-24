package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.app.Application
import android.net.Uri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.HTMLUtil

class DeepLinkViewModel(application: Application) : BaseViewModel<DeepLinkViewModel.InitData, Unit>(application) {

    private val shortcutRepository = ShortcutRepository()

    override fun initViewState() = Unit

    private val url: Uri
        get() = initData.url!!

    override fun onInitializationStarted(data: InitData) {
        if (data.url == null) {
            showMessageDialog(
                Localizable.create { context ->
                    HTMLUtil.format(context.getString(R.string.instructions_deep_linking, EXAMPLE_URL))
                }
            )
            return
        }
        finalizeInitialization()
    }

    private fun showMessageDialog(message: Localizable) {
        emitEvent(
            ViewModelEvent.ShowDialog { context ->
                DialogBuilder(context)
                    .message(message)
                    .positive(R.string.dialog_ok)
                    .dismissListener {
                        onMessageDialogDismissed()
                    }
                    .showIfPossible()
            }
        )
    }

    private fun onMessageDialogDismissed() {
        finish(skipAnimation = true)
    }

    override fun onInitialized() {
        val shortcutIdOrName = getShortcutNameOrId()

        shortcutRepository.getShortcutByNameOrId(shortcutIdOrName)
            .subscribe(
                { shortcut ->
                    executeShortcut(shortcut.id, getVariableValues())
                    finish(skipAnimation = true)
                },
                {
                    showMessageDialog(StringResLocalizable(R.string.error_shortcut_not_found_for_deep_link, shortcutIdOrName))
                }
            )
            .attachTo(destroyer)
    }

    private fun executeShortcut(shortcutId: String, variableValues: Map<String, String>) {
        openActivity(
            ExecuteActivity.IntentBuilder(shortcutId)
                .variableValues(variableValues)
        )
    }

    private fun getShortcutNameOrId(): String =
        url
            .host
            ?.takeUnless { it == "deep-link" }
            ?: url.lastPathSegment
            ?: ""

    private fun getVariableValues(): Map<String, String> =
        url.queryParameterNames
            .filterNot { it.isEmpty() }
            .associateWith { key ->
                url.getQueryParameter(key) ?: ""
            }

    data class InitData(
        val url: Uri?,
    )

    companion object {
        private const val EXAMPLE_URL = "http-shortcuts://<b>&lt;Name/ID of Shortcut&gt;</b>"
    }
}
