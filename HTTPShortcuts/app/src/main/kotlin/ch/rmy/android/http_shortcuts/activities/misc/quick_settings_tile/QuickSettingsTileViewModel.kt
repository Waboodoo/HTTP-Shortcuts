package ch.rmy.android.http_shortcuts.activities.misc.quick_settings_tile

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

class QuickSettingsTileViewModel(application: Application) : BaseViewModel<Unit, QuickSettingsTileViewState>(application) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var shortcuts: List<Shortcut>

    override fun initViewState() = QuickSettingsTileViewState()

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch {
            shortcuts = shortcutRepository.getShortcuts()
                .filter {
                    it.quickSettingsTileShortcut
                }
            finalizeInitialization()
        }
    }

    override fun onInitialized() {
        when (shortcuts.size) {
            0 -> showInstructions()
            1 -> executeShortcut(shortcuts.first().id)
            else -> showSelectionDialog()
        }
    }

    internal fun executeShortcut(shortcutId: ShortcutId) {
        openActivity(ExecuteActivity.IntentBuilder(shortcutId).trigger(ShortcutTriggerType.QUICK_SETTINGS_TILE))
        finish(skipAnimation = true)
    }

    private fun showInstructions() {
        updateViewState {
            copy(
                dialogState = QuickSettingsTileDialogState.Instructions,
            )
        }
    }

    private fun showSelectionDialog() {
        updateViewState {
            copy(
                dialogState = QuickSettingsTileDialogState.PickShortcut(
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
