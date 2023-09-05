package ch.rmy.android.http_shortcuts.activities.misc.voice

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel
@Inject
constructor(
    application: Application,
    private val shortcutRepository: ShortcutRepository,
) : BaseViewModel<VoiceViewModel.InitData, VoiceViewState>(application) {

    override suspend fun initialize(data: InitData): VoiceViewState {
        if (data.shortcutName == null) {
            terminateInitialization()
        }
        try {
            val shortcut = shortcutRepository.getShortcutByNameOrId(shortcutName)
            executeShortcut(shortcut.id)
            terminateInitialization()
        } catch (e: NoSuchElementException) {
            return VoiceViewState(
                shortcutNotFound = true,
            )
        }
    }

    private val shortcutName
        get() = initData.shortcutName!!

    private suspend fun executeShortcut(shortcutId: ShortcutId) {
        sendIntent(
            ExecuteActivity.IntentBuilder(shortcutId)
                .trigger(ShortcutTriggerType.VOICE)
        )
    }

    fun onDialogDismissed() = runAction {
        finish(skipAnimation = true)
    }

    data class InitData(
        val shortcutName: String?,
    )
}
