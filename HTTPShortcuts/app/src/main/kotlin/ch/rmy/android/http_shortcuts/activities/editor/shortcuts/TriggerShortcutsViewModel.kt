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
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager.getCodeFromTriggeredShortcutIds
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager.getTriggeredShortcutIdsFromCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

class TriggerShortcutsViewModel(application: Application) :
    BaseViewModel<TriggerShortcutsViewModel.InitData, TriggerShortcutsViewState>(application) {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    private val currentShortcutId
        get() = initData.currentShortcutId

    private var shortcutsInitialized = false
    private lateinit var shortcuts: List<Shortcut>

    private var shortcutIdsInUse = emptyList<ShortcutListItemId>()
        set(value) {
            if (field == value) {
                return
            }
            field = value
            updateViewState {
                copy(
                    shortcuts = value.map(::getShortcutListItem),
                )
            }
            launchWithProgressTracking {
                temporaryShortcutRepository.setCode(
                    onPrepare = getCodeFromTriggeredShortcutIds(value.map { it.shortcutId }),
                    onSuccess = "",
                    onFailure = "",
                )
            }
        }

    override fun onInitializationStarted(data: InitData) {
        viewModelScope.launch {
            shortcutRepository.getObservableShortcuts()
                .collect { shortcuts ->
                    this@TriggerShortcutsViewModel.shortcuts = shortcuts
                    if (!shortcutsInitialized) {
                        shortcutsInitialized = true
                        viewModelScope.launch {
                            try {
                                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                                initViewStateFromShortcut(temporaryShortcut)
                                finalizeInitialization()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                onInitializationError(e)
                            }
                        }
                    }
                }
        }
    }

    private fun initViewStateFromShortcut(shortcut: Shortcut) {
        shortcutIdsInUse = getTriggeredShortcutIdsFromCode(shortcut.codeOnPrepare)
            .mapIndexed { index, shortcutId -> ShortcutListItemId(shortcutId, id = index.toString()) }
    }

    override fun initViewState() = TriggerShortcutsViewState(
        shortcuts = shortcutIdsInUse.map(::getShortcutListItem),
    )

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

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onShortcutMoved(shortcutId1: ShortcutListItemId, shortcutId2: ShortcutListItemId) {
        shortcutIdsInUse = shortcutIdsInUse.swapped(shortcutId1, shortcutId2) { this }
    }

    fun onAddButtonClicked() {
        updateDialogState(
            TriggerShortcutsDialogState.AddShortcuts(
                shortcuts = shortcuts
                    .filter { it.id != currentShortcutId }
                    .map(Shortcut::toShortcutPlaceholder),
            )
        )
    }

    fun onAddShortcutDialogConfirmed(shortcutIds: List<ShortcutId>) {
        updateDialogState(null)
        shortcutIdsInUse = shortcutIdsInUse.plus(
            shortcutIds.map { shortcutId ->
                ShortcutListItemId(shortcutId, newUUID())
            }
        )
    }

    fun onRemoveShortcutDialogConfirmed() {
        val id = (currentViewState?.dialogState as? TriggerShortcutsDialogState.DeleteShortcut)?.id ?: return
        updateDialogState(null)
        shortcutIdsInUse = shortcutIdsInUse.filter { it != id }
    }

    fun onShortcutClicked(id: ShortcutListItemId) {
        updateDialogState(
            TriggerShortcutsDialogState.DeleteShortcut(
                id = id,
                name = shortcuts.firstOrNull { it.id == id.shortcutId }?.name,
            )
        )
    }

    fun onDismissDialog() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: TriggerShortcutsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(
        val currentShortcutId: ShortcutId?,
    )
}
