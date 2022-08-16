package ch.rmy.android.http_shortcuts.data.domains.pending_executions

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.utils.Optional
import ch.rmy.android.http_shortcuts.data.domains.getPendingExecution
import ch.rmy.android.http_shortcuts.data.domains.getPendingExecutions
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date
import javax.inject.Inject

class PendingExecutionsRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    fun getPendingExecution(id: String): Single<PendingExecutionModel> =
        queryItem {
            getPendingExecution(id)
        }

    fun getObservablePendingExecutions(): Observable<List<PendingExecutionModel>> =
        observeQuery {
            getPendingExecutions()
        }

    fun createPendingExecution(
        shortcutId: ShortcutId,
        resolvedVariables: Map<VariableKey, String> = emptyMap(),
        tryNumber: Int = 0,
        waitUntil: Date? = null,
        requiresNetwork: Boolean = false,
        recursionDepth: Int = 0,
    ) =
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

    fun removePendingExecution(executionId: String) =
        commitTransaction {
            getPendingExecution(executionId)
                .findAll()
                .deleteAllFromRealm()
        }

    fun removePendingExecutionsForShortcut(shortcutId: ShortcutId) =
        commitTransaction {
            getPendingExecutions(shortcutId)
                .findAll()
                .deleteAllFromRealm()
        }

    fun getNextPendingExecution(withNetworkConstraints: Boolean): Single<Optional<PendingExecutionModel>> =
        query {
            getPendingExecutions(waitForNetwork = withNetworkConstraints)
        }
            .map {
                Optional(it.firstOrNull())
            }

    fun removeAllPendingExecutions() =
        commitTransaction {
            getPendingExecutions()
                .findAll()
                .deleteAllFromRealm()
        }
}
