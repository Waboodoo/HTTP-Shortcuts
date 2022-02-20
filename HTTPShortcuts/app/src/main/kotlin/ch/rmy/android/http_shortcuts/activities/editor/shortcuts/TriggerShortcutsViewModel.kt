package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.app.Application
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager.getCodeFromTriggeredShortcutIds
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager.getTriggeredShortcutIdsFromCode

class TriggerShortcutsViewModel(application: Application) :
    BaseViewModel<TriggerShortcutsViewModel.InitData, TriggerShortcutsViewState>(application) {

    private val currentShortcutId
        get() = initData.currentShortcutId

    private var shortcutInitialized = false
    private lateinit var shortcuts: List<Shortcut>

    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val shortcutRepository = ShortcutRepository()

    private var shortcutIdsInUse = emptyList<String>()
        set(value) {
            field = value
            updateViewState {
                copy(
                    shortcuts = value.map(::getShortcutListItem)
                        .ifEmpty { listOf(ShortcutListItem.EmptyState) },
                )
            }
            performOperation(
                temporaryShortcutRepository.setCodeOnPrepare(
                    getCodeFromTriggeredShortcutIds(value)
                )
            )
        }

    override fun onInitializationStarted(data: InitData) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = TriggerShortcutsViewState()

    override fun onInitialized() {
        shortcutRepository.getObservableShortcuts()
            .subscribe { shortcuts ->
                this.shortcuts = shortcuts
                if (!shortcutInitialized) {
                    shortcutInitialized = true
                    temporaryShortcutRepository.getTemporaryShortcut()
                        .subscribe(
                            ::initViewStateFromShortcut,
                            ::onInitializationError,
                        )
                        .attachTo(destroyer)
                }
            }
            .attachTo(destroyer)
    }

    private fun initViewStateFromShortcut(shortcut: Shortcut) {
        shortcutIdsInUse = getTriggeredShortcutIdsFromCode(shortcut.codeOnPrepare)
    }

    private fun getShortcutListItem(shortcutId: String) =
        shortcuts
            .firstOrNull { shortcut -> shortcut.id == shortcutId }
            ?.let { shortcutPlaceholder ->
                ShortcutListItem.Shortcut(
                    id = shortcutId,
                    name = shortcutPlaceholder.name.toLocalizable(),
                    icon = shortcutPlaceholder.icon,
                )
            }
            ?: ShortcutListItem.Shortcut(
                id = shortcutId,
                name = Localizable.create { context ->
                    SpannableString(context.getString(R.string.placeholder_deleted_shortcut))
                        .apply {
                            setSpan(StyleSpan(Typeface.ITALIC), 0, length, 0)
                        }
                },
                icon = ShortcutIcon.NoIcon,
            )

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onShortcutMoved(shortcutId1: String, shortcutId2: String) {
        shortcutIdsInUse = shortcutIdsInUse.swapped(shortcutId1, shortcutId2) { this }
    }

    fun onAddButtonClicked() {
        val shortcutIdsInUse = shortcutIdsInUse
        val placeholders = shortcuts
            .filter { it.id != currentShortcutId && it.id !in shortcutIdsInUse }
            .map(ShortcutPlaceholder::fromShortcut)

        if (placeholders.isEmpty()) {
            showNoShortcutsError()
        } else {
            emitEvent(TriggerShortcutsEvent.ShowShortcutPickerForAdding(placeholders))
        }
    }

    private fun showNoShortcutsError() {
        emitEvent(
            ViewModelEvent.ShowDialog { context ->
                DialogBuilder(context)
                    .title(R.string.title_add_trigger_shortcut)
                    .message(R.string.error_add_trigger_shortcut_no_shortcuts)
                    .positive(R.string.dialog_ok)
                    .showIfPossible()
            }
        )
    }

    fun onAddShortcutDialogConfirmed(shortcutId: String) {
        shortcutIdsInUse = shortcutIdsInUse.plus(shortcutId)
    }

    fun onRemoveShortcutDialogConfirmed(shortcutId: String) {
        shortcutIdsInUse = shortcutIdsInUse.filter { it != shortcutId }
    }

    fun onShortcutClicked(shortcutId: String) {
        val shortcut = shortcuts
            .firstOrNull { it.id == shortcutId }
            ?: return
        emitEvent(TriggerShortcutsEvent.ShowRemoveShortcutDialog(shortcutId, shortcut.name))
    }

    data class InitData(
        val currentShortcutId: String?,
    )
}
