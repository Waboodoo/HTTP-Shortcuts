package ch.rmy.android.http_shortcuts.activities.misc.voice

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import com.afollestad.materialdialogs.callbacks.onCancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class VoiceViewModel(application: Application) : BaseViewModel<VoiceViewModel.InitData, VoiceViewState>(application), WithDialog {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = VoiceViewState()

    private val shortcutName
        get() = initData.shortcutName!!

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: InitData) {
        if (data.shortcutName == null) {
            finish(skipAnimation = true)
            return
        }
        finalizeInitialization()
    }

    private fun showMessageDialog(message: Localizable) {
        dialogState = createDialogState {
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
        viewModelScope.launch {
            try {
                val shortcut = shortcutRepository.getShortcutByNameOrId(shortcutName)
                executeShortcut(shortcut.id)
            } catch (e: NoSuchElementException) {
                showMessageDialog(StringResLocalizable(R.string.error_shortcut_not_found_for_deep_link, shortcutName))
            }
        }
    }

    private fun executeShortcut(shortcutId: ShortcutId) {
        openActivity(ExecuteActivity.IntentBuilder(shortcutId).trigger(ShortcutTriggerType.VOICE))
        finish(skipAnimation = true)
    }

    data class InitData(
        val shortcutName: String?,
    )
}
