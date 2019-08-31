package ch.rmy.android.http_shortcuts.data

import androidx.annotation.CheckResult
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import io.reactivex.Completable
import java.util.*

object Commons { // TODO: Find better name

    @CheckResult
    fun setVariableValue(variableId: String, value: String): Completable =
        Transactions.commit { realm ->
            Repository.getVariableById(realm, variableId)?.value = value
        }


    @CheckResult
    fun createPendingExecution(
        shortcutId: String,
        resolvedVariables: Map<String, String> = emptyMap(),
        tryNumber: Int = 0,
        waitUntil: Date? = null,
        requiresNetwork: Boolean,
        recursionDepth: Int = 0
    ) =
        Transactions.commit { realm ->
            val alreadyPending = Repository.getShortcutPendingExecution(realm, shortcutId) != null
            if (!alreadyPending) {
                realm.copyToRealm(PendingExecution.createNew(
                    shortcutId,
                    resolvedVariables,
                    tryNumber,
                    waitUntil,
                    requiresNetwork,
                    recursionDepth
                ))
            }
        }

    @CheckResult
    fun removePendingExecution(shortcutId: String) =
        Transactions.commit { realm ->
            Repository.getShortcutPendingExecution(realm, shortcutId)?.deleteFromRealm()
        }

}