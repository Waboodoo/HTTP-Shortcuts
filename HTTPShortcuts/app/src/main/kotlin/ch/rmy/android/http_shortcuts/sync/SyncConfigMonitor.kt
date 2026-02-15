package ch.rmy.android.http_shortcuts.sync

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class SyncConfigMonitor
@Inject
constructor() {
    private val configurationInProgress = MutableStateFlow(emptySet<String>())

    fun isConfigurationInProgress(): Boolean =
        configurationInProgress.value.isNotEmpty()

    suspend fun configurationInProgress() {
        val id = newUUID()
        try {
            configurationInProgress.update {
                it + id
            }
            awaitCancellation()
        } finally {
            configurationInProgress.update {
                it - id
            }
        }
    }
}
