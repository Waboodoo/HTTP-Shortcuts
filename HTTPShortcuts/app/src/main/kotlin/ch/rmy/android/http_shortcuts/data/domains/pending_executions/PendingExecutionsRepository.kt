package ch.rmy.android.http_shortcuts.data.domains.pending_executions

import ch.rmy.android.framework.extensions.plus
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionWithVariablesModel
import java.time.Instant
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PendingExecutionsRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getPendingExecution(id: ExecutionId): PendingExecution = query {
        pendingExecutionDao()
            .getPendingExecution(id)
            .first()
            .toPendingExecution()
    }

    private fun PendingExecutionWithVariablesModel.toPendingExecution() =
        PendingExecution(
            id = pendingExecution.id,
            shortcutId = pendingExecution.shortcutId,
            tryNumber = pendingExecution.tryNumber,
            delayUntil = pendingExecution.delayUntil,
            waitForNetwork = pendingExecution.waitForNetwork,
            recursionDepth = pendingExecution.recursionDepth,
            resolvedVariables = resolvedVariables.associate { it.key to it.value },
            requestCode = pendingExecution.requestCode,
            type = PendingExecutionType.parse(pendingExecution.type) ?: PendingExecutionType.UNKNOWN,
        )

    fun observePendingExecutions(): Flow<List<PendingExecution>> = queryFlow {
        pendingExecutionDao()
            .observePendingExecutions()
            .distinctUntilChanged()
            .map { pendingExecutions ->
                pendingExecutions.map { it.toPendingExecution() }
            }
    }

    suspend fun getPendingExecutionsForShortcut(shortcutId: ShortcutId): List<PendingExecution> = query {
        pendingExecutionDao()
            .getPendingExecutionsForShortcut(shortcutId)
            .map {
                it.toPendingExecution()
            }
    }

    suspend fun createPendingExecution(
        shortcutId: ShortcutId,
        resolvedVariables: Map<VariableKey, String> = emptyMap(),
        tryNumber: Int = 0,
        delay: Duration? = null,
        requiresNetwork: Boolean = false,
        recursionDepth: Int = 0,
        type: PendingExecutionType,
    ) = query {
        pendingExecutionDao()
            .insert(
                PendingExecutionModel(
                    shortcutId = shortcutId,
                    tryNumber = tryNumber,
                    delayUntil = calculateInstant(delay),
                    waitForNetwork = requiresNetwork,
                    recursionDepth = recursionDepth,
                    type = type.name,
                    requestCode = Random.nextInt(10_000),
                    enqueuedAt = Instant.now(),
                ),
                resolvedVariables,
            )
    }

    private fun calculateInstant(delay: Duration?): Instant? {
        if (delay == null || delay <= 0.milliseconds) {
            return null
        }
        return Instant.now() + delay
    }

    suspend fun removePendingExecution(executionId: ExecutionId) = query {
        pendingExecutionDao().delete(executionId)
    }

    suspend fun removePendingExecutionsForShortcut(shortcutId: ShortcutId) = query {
        pendingExecutionDao().deleteForShortcut(shortcutId)
    }

    suspend fun getNextPendingExecution(withNetworkConstraints: Boolean): PendingExecution? = query {
        pendingExecutionDao()
            .run {
                if (withNetworkConstraints) {
                    getNextPendingExecutionWaitingForNetwork()
                } else {
                    getNextPendingExecution()
                }
            }
            ?.toPendingExecution()
    }

    suspend fun removeAllPendingExecutions() = query {
        pendingExecutionDao().deleteAll()
    }
}
