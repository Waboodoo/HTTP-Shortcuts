package ch.rmy.android.http_shortcuts.activities.history

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.history.usecases.CopyHistoryItemUseCase
import ch.rmy.android.http_shortcuts.activities.history.usecases.MapEventsUseCase
import ch.rmy.android.http_shortcuts.data.domains.history.HistoryRepository
import ch.rmy.android.http_shortcuts.history.HistoryCleanUpWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@HiltViewModel
class HistoryViewModel
@Inject
constructor(
    application: Application,
    private val historyRepository: HistoryRepository,
    private val mapEvents: MapEventsUseCase,
    private val historyCleanUpStarter: HistoryCleanUpWorker.Starter,
    private val copyHistoryItemUseCase: CopyHistoryItemUseCase,
) : BaseViewModel<Unit, HistoryViewState>(application) {

    override suspend fun initialize(data: Unit): HistoryViewState {
        viewModelScope.launch {
            historyRepository.getObservableHistory(MAX_AGE).collect { events ->
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
        )
    }

    fun onClearHistoryButtonClicked() = runAction {
        withProgressTracking {
            historyRepository.deleteHistory()
            showSnackbar(R.string.message_history_cleared)
        }
    }

    fun onHistoryEventLongPressed(id: String) = runAction {
        val item = viewState.historyItems
            .find { it.id == id }
            ?: skipAction()
        copyHistoryItemUseCase(item)
        showSnackbar(R.string.message_history_event_details_copied)
    }

    companion object {
        private val MAX_AGE = 8.hours
    }
}
