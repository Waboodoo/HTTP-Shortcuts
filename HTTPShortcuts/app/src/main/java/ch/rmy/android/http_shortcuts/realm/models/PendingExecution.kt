package ch.rmy.android.http_shortcuts.realm.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class PendingExecution : RealmObject() {

    @PrimaryKey
    var shortcutId: Long = 0
    @Index
    @Required
    var enqueuedAt: Date = Date()

    var tryNumber: Int = 0
    var waitUntil: Date? = null
    var waitForNetwork: Boolean = false

    var resolvedVariables: RealmList<ResolvedVariable> = RealmList()

    companion object {

        const val FIELD_SHORTCUT_ID = "shortcutId"
        const val FIELD_ENQUEUED_AT = "enqueuedAt"

        fun createNew(
                shortcutId: Long,
                resolvedVariables: Map<String, String> = emptyMap(),
                tryNumber: Int = 0,
                waitUntil: Date? = null,
                waitForNetwork: Boolean = false
        ): PendingExecution {
            val pendingExecution = PendingExecution()

            val resolvedVariableList = RealmList<ResolvedVariable>()
            resolvedVariables.mapTo(resolvedVariableList) {
                ResolvedVariable.createNew(it.key, it.value)
            }

            pendingExecution.resolvedVariables = resolvedVariableList
            pendingExecution.shortcutId = shortcutId
            pendingExecution.tryNumber = tryNumber
            pendingExecution.waitUntil = waitUntil
            pendingExecution.enqueuedAt = Date()
            pendingExecution.waitForNetwork = waitForNetwork
            return pendingExecution
        }
    }

}
