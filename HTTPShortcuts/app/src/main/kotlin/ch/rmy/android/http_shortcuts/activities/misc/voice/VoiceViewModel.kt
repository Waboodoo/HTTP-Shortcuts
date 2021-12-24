package ch.rmy.android.http_shortcuts.activities.misc.voice

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder

class VoiceViewModel(application: Application) : BaseViewModel<VoiceViewModel.InitData, Unit>(application) {

    private val shortcutRepository = ShortcutRepository()

    override fun initViewState() = Unit

    private val shortcutName
        get() = initData.shortcutName!!

    override fun onInitializationStarted(data: InitData) {
        if (data.shortcutName == null) {
            finish(skipAnimation = true)
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
        shortcutRepository.getShortcutByNameOrId(shortcutName)
            .subscribe(
                { shortcut ->
                    executeShortcut(shortcut.id)
                    finish(skipAnimation = true)
                },
                {
                    showMessageDialog(StringResLocalizable(R.string.error_shortcut_not_found_for_deep_link, shortcutName))
                }
            )
            .attachTo(destroyer)
    }

    private fun executeShortcut(shortcutId: String) {
        openActivity(ExecuteActivity.IntentBuilder(shortcutId))
    }

    data class InitData(
        val shortcutName: String?,
    )
}
