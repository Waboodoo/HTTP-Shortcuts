package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import android.app.Activity
import android.app.Application
import android.app.Dialog
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.misc.share.ShareViewState
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import com.afollestad.materialdialogs.callbacks.onCancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SecondLauncherViewModel(application: Application) : BaseViewModel<Unit, ShareViewState>(application), WithDialog {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var shortcuts: List<Shortcut>

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = ShareViewState()

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
        dialogState = object : DialogState {
            override fun createDialog(activity: Activity, viewModel: WithDialog?): Dialog =
                DialogBuilder(activity)
                    .runFor(shortcuts) { shortcut ->
                        item(name = shortcut.name, shortcutIcon = shortcut.icon) {
                            executeShortcut(shortcut.id)
                        }
                    }
                    .build()
                    .onCancel {
                        finish(skipAnimation = true)
                    }
        }
    }
}
