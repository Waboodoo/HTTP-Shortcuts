package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.debounce

@HiltViewModel
class SyncImportViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val syncRepository: SyncRepository,
) : BaseViewModel<Unit, SyncImportViewState>(application) {
    private lateinit var configFlow: MutableStateFlow<SyncConfig>

    @OptIn(FlowPreview::class)
    override suspend fun initialize(data: Unit): SyncImportViewState {
        if (userPreferences.syncType != SyncType.IMPORT) {
            terminateInitialization()
        }

        val config = syncRepository.getConfig(SyncType.IMPORT)
        configFlow = MutableStateFlow(config)
        viewModelScope.launch(Dispatchers.Default) {
            configFlow.drop(1)
                .debounce(300.milliseconds)
                .collectLatest { config ->
                    syncRepository.updateConfig(config)
                }
        }
        return SyncImportViewState(
            schedule = config.schedule,
            password = config.password,
        )
    }

    private fun updateConfig(update: SyncConfig.() -> SyncConfig) {
        configFlow.update { update(it) }
    }

    fun onPasswordChanged(password: String) = runAction {
        updateViewState {
            copy(password = password)
        }
        updateConfig { copy(password = password) }
    }
}
