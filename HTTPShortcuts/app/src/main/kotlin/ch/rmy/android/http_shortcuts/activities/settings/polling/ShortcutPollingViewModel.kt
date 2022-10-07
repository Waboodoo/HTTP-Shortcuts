package ch.rmy.android.http_shortcuts.activities.settings.polling

import android.app.Application
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import ch.rmy.android.framework.extensions.*
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.settings.polling.models.ShortcutListItem
import ch.rmy.android.http_shortcuts.activities.settings.polling.models.ShortcutListItemId
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.callbacks.onShow
import javax.inject.Inject

class ShortcutPollingViewModel(application: Application) :
    BaseViewModel<Unit, ShortcutPollingViewState>(application), WithDialog {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

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
                appRepository.setPollingShortcuts(value.map {
                    shortcutRepository.getShortcutById(it.shortcutId).blockingGet()
                })
            )
        }

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = ShortcutPollingViewState()

    override fun onInitialized() {
        shortcutRepository.getShortcuts()
            .subscribe { shortcuts ->
                this.shortcuts = shortcuts
            }
            .attachTo(destroyer)
        appRepository.getPollingShortcuts()
            .subscribe { shortcuts ->
                shortcutIdsInUse = shortcuts
                    .mapIndexed { index, shortcut ->
                        ShortcutListItemId(
                            shortcut.id,
                            id = index.toString()
                        )
                    }
            }
            .attachTo(destroyer)
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

    fun onShortcutMoved(shortcutId1: ShortcutListItemId, shortcutId2: ShortcutListItemId) {
        shortcutIdsInUse = shortcutIdsInUse.swapped(shortcutId1, shortcutId2) { this }
    }

    fun onAddButtonClicked() {
        val placeholders = shortcuts.map(ShortcutPlaceholder::fromShortcut)

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

}
