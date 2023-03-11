package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.Date

@RealmClass
open class PendingExecution(
    @PrimaryKey
    var id: ExecutionId = "",
    var shortcutId: ShortcutId = "",
    @Suppress("unused")
    @Index
    @Required
    var enqueuedAt: Date = Date(),
    var tryNumber: Int = 0,
    var waitUntil: Date? = null,
    @Suppress("unused")
    var waitForNetwork: Boolean = false,
    var recursionDepth: Int = 0,
    var resolvedVariables: RealmList<ResolvedVariable> = RealmList(),
    type: PendingExecutionType = PendingExecutionType.UNKNOWN,
    var requestCode: Int = 0,
) : RealmModel {

    @Required
    private var scheduleType: String = PendingExecutionType.UNKNOWN.name

    var type: PendingExecutionType
        get() = PendingExecutionType.parse(scheduleType)
        set(value) {
            scheduleType = value.name
        }

    init {
        scheduleType = type.name
    }

    companion object {

        const val FIELD_ID = "id"
        const val FIELD_SHORTCUT_ID = "shortcutId"
        const val FIELD_ENQUEUED_AT = "enqueuedAt"
        const val FIELD_WAIT_FOR_NETWORK = "waitForNetwork"

        fun createNew(
            shortcutId: ShortcutId,
            resolvedVariables: Map<VariableKey, String> = emptyMap(),
            tryNumber: Int = 0,
            waitUntil: Date? = null,
            waitForNetwork: Boolean = false,
            recursionDepth: Int = 0,
            type: PendingExecutionType,
            requestCode: Int,
        ): PendingExecution {
            val resolvedVariableList = RealmList<ResolvedVariable>()
            resolvedVariables.mapTo(resolvedVariableList) {
                ResolvedVariable(it.key, it.value)
            }

            return PendingExecution(
                id = UUIDUtils.newUUID(),
                resolvedVariables = resolvedVariableList,
                shortcutId = shortcutId,
                tryNumber = tryNumber,
                waitUntil = waitUntil,
                enqueuedAt = Date(),
                waitForNetwork = waitForNetwork,
                recursionDepth = recursionDepth,
                type = type,
                requestCode = requestCode,
            )
        }
    }
}
