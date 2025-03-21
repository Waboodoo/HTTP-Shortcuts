package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.app.Application
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItem
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItemId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager.getCodeFromTriggeredShortcutIds
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager.getTriggeredShortcutIdsFromCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class TriggerShortcutsViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val shortcutRepository: ShortcutRepository,
) : BaseViewModel<TriggerShortcutsViewModel.InitData, TriggerShortcutsViewState>(application) {

    private val currentShortcutId
        get() = initData.currentShortcutId

    private lateinit var shortcuts: List<Shortcut>

    private var shortcutIdsInUse = emptyList<ShortcutListItemId>()
        set(value) {
            if (field == value) {
                return
            }
            field = value
            runAction {
                updateViewState {
                    copy(
                        shortcuts = value.map(::getShortcutListItem),
                    )
                }
                withProgressTracking {
                    temporaryShortcutRepository.setCode(
                        onPrepare = getCodeFromTriggeredShortcutIds(value.map { it.shortcutId }),
                        onSuccess = "",
                        onFailure = "",
                    )
                }
            }
        }

    override suspend fun initialize(data: InitData): TriggerShortcutsViewState {
        val shortcutsFlow = shortcutRepository.observeShortcuts()
        this.shortcuts = shortcutsFlow.first()
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        shortcutIdsInUse = getTriggeredShortcutIdsFromCode(shortcut.codeOnPrepare)
            .mapIndexed { index, shortcutId -> ShortcutListItemId(shortcutId, id = index.toString()) }

        viewModelScope.launch {
            shortcutsFlow.collect {
                this@TriggerShortcutsViewModel.shortcuts = it
            }
        }

        return TriggerShortcutsViewState(
            shortcuts = shortcutIdsInUse.map(::getShortcutListItem),
        )
    }

    private fun getShortcutListItem(id: ShortcutListItemId) =
        shortcuts
            .firstOrNull { shortcut -> shortcut.id == id.shortcutId }
            ?.let { shortcutPlaceholder ->
                ShortcutListItem(
                    id = id,
                    name = shortcutPlaceholder.name.toLocalizable(),
                    icon = shortcutPlaceholder.icon,
                )
            }
            ?: ShortcutListItem(
                id = id,
                name = Localizable.create { context ->
                    SpannableString(context.getString(R.string.placeholder_deleted_shortcut))
                        .apply {
                            setSpan(StyleSpan(Typeface.ITALIC), 0, length, 0)
                        }
                },
                icon = ShortcutIcon.NoIcon,
            )

    fun onShortcutMoved(shortcutId1: ShortcutListItemId, shortcutId2: ShortcutListItemId) = runAction {
        shortcutIdsInUse = shortcutIdsInUse.swapped(shortcutId1, shortcutId2) { this }
    }

    fun onAddButtonClicked() = runAction {
        updateDialogState(
            TriggerShortcutsDialogState.AddShortcuts(
                shortcuts = shortcuts
                    .filter { it.id != currentShortcutId }
                    .map(Shortcut::toShortcutPlaceholder),
            ),
        )
    }

    fun onAddShortcutDialogConfirmed(shortcutIds: List<ShortcutId>) = runAction {
        updateDialogState(null)
        shortcutIdsInUse = shortcutIdsInUse.plus(
            shortcutIds.map { shortcutId ->
                ShortcutListItemId(shortcutId, newUUID())
            },
        )
    }

    fun onRemoveShortcutDialogConfirmed() = runAction {
        val id = (viewState.dialogState as? TriggerShortcutsDialogState.DeleteShortcut)?.id ?: skipAction()
        updateDialogState(null)
        shortcutIdsInUse = shortcutIdsInUse.filter { it != id }
    }

    fun onShortcutClicked(id: ShortcutListItemId) = runAction {
        updateDialogState(
            TriggerShortcutsDialogState.DeleteShortcut(
                id = id,
                name = shortcuts.firstOrNull { it.id == id.shortcutId }?.name,
            ),
        )
    }

    fun onDismissDialog() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: TriggerShortcutsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(
        val currentShortcutId: ShortcutId?,
    )
}
