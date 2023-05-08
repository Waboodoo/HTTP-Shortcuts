package ch.rmy.android.http_shortcuts.activities.misc.voice

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import kotlinx.coroutines.launch
import javax.inject.Inject

class VoiceViewModel(application: Application) : BaseViewModel<VoiceViewModel.InitData, VoiceViewState>(application) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = VoiceViewState()

    private val shortcutName
        get() = initData.shortcutName!!

    override fun onInitializationStarted(data: InitData) {
        if (data.shortcutName == null) {
            finish(skipAnimation = true)
            return
        }
        finalizeInitialization()
    }

    override fun onInitialized() {
        viewModelScope.launch {
            try {
                val shortcut = shortcutRepository.getShortcutByNameOrId(shortcutName)
                executeShortcut(shortcut.id)
            } catch (e: NoSuchElementException) {
                updateViewState {
                    copy(shortcutNotFound = true)
                }
            }
        }
    }

    private fun executeShortcut(shortcutId: ShortcutId) {
        openActivity(ExecuteActivity.IntentBuilder(shortcutId).trigger(ShortcutTriggerType.VOICE))
        finish(skipAnimation = true)
    }

    fun onDialogDismissed() {
        finish(skipAnimation = true)
    }

    data class InitData(
        val shortcutName: String?,
    )
}
