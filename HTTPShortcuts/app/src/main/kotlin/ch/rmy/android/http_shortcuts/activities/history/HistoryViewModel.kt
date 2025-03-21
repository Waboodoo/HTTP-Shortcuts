package ch.rmy.android.http_shortcuts.activities.history

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.history.usecases.CopyHistoryItemUseCase
import ch.rmy.android.http_shortcuts.activities.history.usecases.MapEventsUseCase
import ch.rmy.android.http_shortcuts.data.domains.history.HistoryRepository
import ch.rmy.android.http_shortcuts.history.HistoryCleanUpWorker
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel
@Inject
constructor(
    application: Application,
    private val historyRepository: HistoryRepository,
    private val mapEvents: MapEventsUseCase,
    private val historyCleanUpStarter: HistoryCleanUpWorker.Starter,
    private val copyHistoryItemUseCase: CopyHistoryItemUseCase,
    private val settings: Settings,
) : BaseViewModel<Unit, HistoryViewState>(application) {

    override suspend fun initialize(data: Unit): HistoryViewState {
        viewModelScope.launch {
            historyRepository.observeHistory(MAX_AGE).collect { events ->
                updateViewState {
                    copy(historyItems = mapEvents(events))
                }
            }
        }
        viewModelScope.launch {
            historyCleanUpStarter()
        }
        return HistoryViewState(
            historyItems = emptyList(),
            useRelativeTimes = settings.useRelativeTimesInHistory,
        )
    }

    fun onClearHistoryButtonClicked() = runAction {
        withProgressTracking {
            historyRepository.deleteHistory()
            showSnackbar(R.string.message_history_cleared)
        }
    }

    fun onHistoryEventLongPressed(id: Int) = runAction {
        val item = viewState.historyItems
            .find { it.id == id }
            ?: skipAction()
        copyHistoryItemUseCase(item)
        showSnackbar(R.string.message_history_event_details_copied)
    }

    fun onTimeModeToggleButtonClicked() = runAction {
        val useRelative = !getCurrentViewState().useRelativeTimes
        updateViewState {
            settings.useRelativeTimesInHistory = useRelative
            copy(useRelativeTimes = useRelative)
        }
        showSnackbar(if (useRelative) R.string.message_history_switched_to_relative_times else R.string.message_history_switched_to_absolute_times)
    }

    companion object {
        private val MAX_AGE = 8.hours
    }
}
