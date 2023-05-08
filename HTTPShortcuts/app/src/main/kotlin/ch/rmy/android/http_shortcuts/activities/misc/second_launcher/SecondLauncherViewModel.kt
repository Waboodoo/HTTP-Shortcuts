package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import kotlinx.coroutines.launch
import javax.inject.Inject

class SecondLauncherViewModel(application: Application) : BaseViewModel<Unit, SecondLauncherViewState>(application) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var shortcuts: List<Shortcut>

    override fun initViewState() = SecondLauncherViewState()

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch {
            shortcuts = shortcutRepository.getShortcuts()
                .filter {
                    it.secondaryLauncherShortcut
                }
            finalizeInitialization()
        }
    }

    override fun onInitialized() {
        shortcuts.singleOrNull()
            ?.let {
                executeShortcut(it.id)
            }
            ?: run {
                showSelectionDialog()
            }
    }

    internal fun executeShortcut(shortcutId: ShortcutId) {
        openActivity(ExecuteActivity.IntentBuilder(shortcutId).trigger(ShortcutTriggerType.SECONDARY_LAUNCHER_APP))
        finish(skipAnimation = true)
    }

    private fun showSelectionDialog() {
        updateViewState {
            copy(
                dialogState = SecondLauncherDialogState.PickShortcut(
                    shortcuts.map { it.toShortcutPlaceholder() },
                )
            )
        }
    }

    fun onShortcutSelected(shortcutId: ShortcutId) {
        executeShortcut(shortcutId)
    }

    fun onDialogDismissed() {
        finish(skipAnimation = true)
    }
}
