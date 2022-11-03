package ch.rmy.android.http_shortcuts.data.domains.pending_executions

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getPendingExecution
import ch.rmy.android.http_shortcuts.data.domains.getPendingExecutions
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class PendingExecutionsRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun getPendingExecution(id: ExecutionId): PendingExecutionModel =
        queryItem {
            getPendingExecution(id)
        }

    fun getObservablePendingExecutions(): Flow<List<PendingExecutionModel>> =
        observeQuery {
            getPendingExecutions()
        }

    suspend fun createPendingExecution(
        shortcutId: ShortcutId,
        resolvedVariables: Map<VariableKey, String> = emptyMap(),
        tryNumber: Int = 0,
        waitUntil: Date? = null,
        requiresNetwork: Boolean = false,
        recursionDepth: Int = 0,
    ) {
        commitTransaction {
            copy(
                PendingExecutionModel.createNew(
                    shortcutId,
                    resolvedVariables,
                    tryNumber,
                    waitUntil,
                    requiresNetwork,
                    recursionDepth,
                )
            )
        }
    }

    suspend fun removePendingExecution(executionId: ExecutionId) {
        commitTransaction {
            getPendingExecution(executionId)
                .findAll()
                .deleteAllFromRealm()
        }
    }

    suspend fun removePendingExecutionsForShortcut(shortcutId: ShortcutId) =
        commitTransaction {
            getPendingExecutions(shortcutId)
                .findAll()
                .deleteAllFromRealm()
        }

    suspend fun getNextPendingExecution(withNetworkConstraints: Boolean): PendingExecutionModel? =
        query {
            getPendingExecutions(waitForNetwork = withNetworkConstraints)
        }
            .firstOrNull()

    suspend fun removeAllPendingExecutions() {
        commitTransaction {
            getPendingExecutions()
                .findAll()
                .deleteAllFromRealm()
        }
    }
}
