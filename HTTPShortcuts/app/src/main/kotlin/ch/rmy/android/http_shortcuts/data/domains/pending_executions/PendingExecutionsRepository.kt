package ch.rmy.android.http_shortcuts.data.domains.pending_executions

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.extensions.plus
import ch.rmy.android.http_shortcuts.data.domains.getPendingExecution
import ch.rmy.android.http_shortcuts.data.domains.getPendingExecutions
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class PendingExecutionsRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun getPendingExecution(id: ExecutionId): PendingExecution =
        queryItem {
            getPendingExecution(id)
        }

    fun getObservablePendingExecutions(): Flow<List<PendingExecution>> =
        observeQuery {
            getPendingExecutions()
        }

    suspend fun getPendingExecutionsForShortcut(shortcutId: ShortcutId): List<PendingExecution> =
        query {
            getPendingExecutions(shortcutId)
        }

    suspend fun createPendingExecution(
        shortcutId: ShortcutId,
        resolvedVariables: Map<VariableKey, String> = emptyMap(),
        tryNumber: Int = 0,
        delay: Duration? = null,
        requiresNetwork: Boolean = false,
        recursionDepth: Int = 0,
        type: PendingExecutionType,
    ) {
        commitTransaction {
            val maxRequestCode = getPendingExecutions()
                .find()
                .maxOfOrNull { it.requestCode }
            copy(
                PendingExecution.createNew(
                    shortcutId,
                    resolvedVariables,
                    tryNumber,
                    calculateInstant(delay),
                    requiresNetwork,
                    recursionDepth,
                    type,
                    requestCode = (maxRequestCode ?: 0) + 1,
                )
            )
        }
    }

    private fun calculateInstant(delay: Duration?): Instant? {
        if (delay == null || delay <= 0.milliseconds) {
            return null
        }
        return Instant.now() + delay
    }

    suspend fun removePendingExecution(executionId: ExecutionId) {
        commitTransaction {
            getPendingExecution(executionId).deleteAll()
        }
    }

    suspend fun removePendingExecutionsForShortcut(shortcutId: ShortcutId) =
        commitTransaction {
            getPendingExecutions(shortcutId).deleteAll()
        }

    suspend fun getNextPendingExecution(withNetworkConstraints: Boolean): PendingExecution? =
        query {
            getPendingExecutions(waitForNetwork = withNetworkConstraints)
        }
            .firstOrNull()

    suspend fun removeAllPendingExecutions() {
        commitTransaction {
            getPendingExecutions().deleteAll()
        }
    }
}
