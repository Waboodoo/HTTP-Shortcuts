package ch.rmy.android.http_shortcuts.sync

import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest

class ObserveSyncReplaceUseCase
@Inject
constructor(
    private val userPreferences: UserPreferences,
    private val syncRepository: SyncRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Boolean> =
        userPreferences.observeSyncType()
            .transformLatest { syncType ->
                if (syncType == SyncType.IMPORT) {
                    syncRepository.observeConfig(SyncType.IMPORT)
                        .collect { config ->
                            emit(config?.replaceLocal == true)
                        }
                } else {
                    emit(false)
                }
            }
            .distinctUntilChanged()
}
