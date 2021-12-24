package ch.rmy.android.http_shortcuts.data.domains.pending_executions

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.utils.Optional
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getPendingExecution
import ch.rmy.android.http_shortcuts.data.domains.getPendingExecutions
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date

class PendingExecutionsRepository : BaseRepository(RealmFactory.getInstance()) {

    fun getPendingExecution(id: String): Single<PendingExecution> =
        queryItem {
            getPendingExecution(id)
        }

    fun getObservablePendingExecutions(): Observable<List<PendingExecution>> =
        observeQuery {
            getPendingExecutions()
        }

    fun createPendingExecution(
        shortcutId: String,
        resolvedVariables: Map<String, String> = emptyMap(),
        tryNumber: Int = 0,
        waitUntil: Date? = null,
        requiresNetwork: Boolean = false,
        recursionDepth: Int = 0,
    ) =
        commitTransaction {
            copy(
                PendingExecution.createNew(
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

    fun removePendingExecutionsForShortcut(shortcutId: String) =
        commitTransaction {
            getPendingExecutions(shortcutId)
                .findAll()
                .deleteAllFromRealm()
        }

    fun getNextPendingExecution(withNetworkConstraints: Boolean): Single<Optional<PendingExecution>> =
        query {
            getPendingExecutions(waitForNetwork = withNetworkConstraints)
        }
            .map {
                Optional(it.firstOrNull())
            }
}
