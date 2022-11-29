package ch.rmy.android.http_shortcuts.activities.history

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.history.usecases.MapEventsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.history.HistoryRepository
import ch.rmy.android.http_shortcuts.history.HistoryCleanUpWorker
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class HistoryViewModel(application: Application) : BaseViewModel<Unit, HistoryViewState>(application), WithDialog {

    @Inject
    lateinit var historyRepository: HistoryRepository

    @Inject
    lateinit var mapEvents: MapEventsUseCase

    @Inject
    lateinit var historyCleanUpStarter: HistoryCleanUpWorker.Starter

    init {
        getApplicationComponent().inject(this)
    }

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = HistoryViewState(
        historyItems = emptyList(),
    )

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch {
            historyRepository.getObservableHistory(MAX_AGE).collect { events ->
                if (isInitialized) {
                    updateViewState {
                        copy(historyItems = mapEvents(events))
                    }
                } else {
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

    companion object {
        private val MAX_AGE = 8.hours
    }
}
