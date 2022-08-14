package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.app.Application
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import ch.rmy.android.framework.extensions.addOrRemove
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItem
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItemId
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager.getCodeFromTriggeredShortcutIds
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager.getTriggeredShortcutIdsFromCode
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.callbacks.onShow
import javax.inject.Inject

class TriggerShortcutsViewModel(application: Application) :
    BaseViewModel<TriggerShortcutsViewModel.InitData, TriggerShortcutsViewState>(application), WithDialog {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    private val currentShortcutId
        get() = initData.currentShortcutId

    private var shortcutInitialized = false
    private lateinit var shortcuts: List<ShortcutModel>

    private var shortcutIdsInUse = emptyList<ShortcutListItemId>()
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
                    getCodeFromTriggeredShortcutIds(value.map { it.shortcutId })
                )
            )
        }

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

    private fun initViewStateFromShortcut(shortcut: ShortcutModel) {
        shortcutIdsInUse = getTriggeredShortcutIdsFromCode(shortcut.codeOnPrepare)
            .mapIndexed { index, shortcutId -> ShortcutListItemId(shortcutId, id = index.toString()) }
    }

    private fun getShortcutListItem(id: ShortcutListItemId) =
        shortcuts
            .firstOrNull { shortcut -> shortcut.id == id.shortcutId }
            ?.let { shortcutPlaceholder ->
                ShortcutListItem.Shortcut(
                    id = id,
                    name = shortcutPlaceholder.name.toLocalizable(),
                    icon = shortcutPlaceholder.icon,
                )
            }
            ?: ShortcutListItem.Shortcut(
                id = id,
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

    fun onShortcutMoved(shortcutId1: ShortcutListItemId, shortcutId2: ShortcutListItemId) {
        shortcutIdsInUse = shortcutIdsInUse.swapped(shortcutId1, shortcutId2) { this }
    }

    fun onAddButtonClicked() {
        val placeholders = shortcuts
            .filter { it.id != currentShortcutId }
            .map(ShortcutPlaceholder::fromShortcut)

        if (placeholders.isEmpty()) {
            showNoShortcutsError()
        } else {
            showShortcutPickerForAdding(placeholders)
        }
    }

    private fun showNoShortcutsError() {
        dialogState = DialogState.create {
            title(R.string.title_add_trigger_shortcut)
                .message(R.string.error_add_trigger_shortcut_no_shortcuts)
                .positive(R.string.dialog_ok)
                .build()
        }
    }

    private fun showShortcutPickerForAdding(placeholders: List<ShortcutPlaceholder>) {
        val selectedShortcutIds = mutableSetOf<ShortcutId>()
        var onSelectionChanged: () -> Unit = {}
        dialogState = DialogState.create {
            title(R.string.title_add_trigger_shortcut)
                .runFor(placeholders) { shortcut ->
                    checkBoxItem(
                        name = shortcut.name,
                        shortcutIcon = shortcut.icon,
                        checked = { shortcut.id in selectedShortcutIds },
                    ) { checked ->
                        selectedShortcutIds.addOrRemove(shortcut.id, checked)
                        onSelectionChanged()
                    }
                }
                .positive(R.string.dialog_ok) {
                    onAddShortcutDialogConfirmed(
                        placeholders
                            .map { it.id }
                            .filter { it in selectedShortcutIds }
                    )
                }
                .build()
                .onShow { dialog ->
                    val okButton = dialog.getActionButton(WhichButton.POSITIVE)
                    onSelectionChanged = {
                        okButton.isEnabled = selectedShortcutIds.isNotEmpty()
                    }
                        .apply { invoke() }
                }
        }
    }

    private fun onAddShortcutDialogConfirmed(shortcutIds: List<ShortcutId>) {
        val offset = shortcutIdsInUse.size
        shortcutIdsInUse = shortcutIdsInUse.plus(
            shortcutIds.mapIndexed { index, shortcutId ->
                ShortcutListItemId(shortcutId, (index + offset).toString())
            }
        )
    }

    private fun onRemoveShortcutDialogConfirmed(id: ShortcutListItemId) {
        shortcutIdsInUse = shortcutIdsInUse.filter { it != id }
    }

    fun onShortcutClicked(id: ShortcutListItemId) {
        val message = shortcuts
            .firstOrNull { it.id == id.shortcutId }
            ?.let { shortcut ->
                StringResLocalizable(R.string.message_remove_trigger_shortcut, shortcut.name)
            }
            ?: StringResLocalizable(R.string.message_remove_deleted_trigger_shortcut)
        showRemoveShortcutDialog(id, message)
    }

    private fun showRemoveShortcutDialog(id: ShortcutListItemId, message: Localizable) {
        dialogState = DialogState.create {
            title(R.string.title_remove_trigger_shortcut)
                .message(message)
                .positive(R.string.dialog_remove) {
                    onRemoveShortcutDialogConfirmed(id)
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
    }

    data class InitData(
        val currentShortcutId: ShortcutId?,
    )
}
