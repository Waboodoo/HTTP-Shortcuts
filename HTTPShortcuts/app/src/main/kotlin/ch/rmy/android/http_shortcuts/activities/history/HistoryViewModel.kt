package ch.rmy.android.http_shortcuts.activities.history

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.history.usecases.CopyHistoryItemUseCase
import ch.rmy.android.http_shortcuts.activities.history.usecases.MapEventsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.history.HistoryRepository
import ch.rmy.android.http_shortcuts.history.HistoryCleanUpWorker
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class HistoryViewModel(application: Application) : BaseViewModel<Unit, HistoryViewState>(application) {

    @Inject
    lateinit var historyRepository: HistoryRepository

    @Inject
    lateinit var mapEvents: MapEventsUseCase

    @Inject
    lateinit var historyCleanUpStarter: HistoryCleanUpWorker.Starter

    @Inject
    lateinit var copyHistoryItemUseCase: CopyHistoryItemUseCase

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = HistoryViewState(
        historyItems = emptyList(),
    )

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch {
            historyRepository.getObservableHistory(MAX_AGE).collect { events ->
                updateViewState {
                    copy(historyItems = mapEvents(events))
                }
                if (!isInitialized) {
                    finalizeInitialization()
                }
            }
        }
    }

    override fun onInitialized() {
        historyCleanUpStarter()
    }

    fun onClearHistoryButtonClicked() {
        launchWithProgressTracking {
            historyRepository.deleteHistory()
            showSnackbar(R.string.message_history_cleared)
        }
    }

    fun onHistoryEventLongPressed(id: String) {
        doWithViewState { viewState ->
            val item = viewState.historyItems
                .find { it.id == id }
                ?: return@doWithViewState
            copyHistoryItemUseCase(item)
            showSnackbar(R.string.message_history_event_details_copied)
        }
    }

    companion object {
        private val MAX_AGE = 8.hours
    }
}
