package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.toInstant
import ch.rmy.android.framework.extensions.toRealmInstant
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import java.time.Instant

class PendingExecution() : RealmObject {

    constructor(
        id: ExecutionId = "",
        shortcutId: ShortcutId = "",
        tryNumber: Int = 0,
        delayUntil: Instant? = null,
        waitForNetwork: Boolean = false,
        recursionDepth: Int = 0,
        resolvedVariables: RealmList<ResolvedVariable> = realmListOf(),
        type: PendingExecutionType = PendingExecutionType.UNKNOWN,
        requestCode: Int = 0,
    ) : this() {
        this.id = id
        this.shortcutId = shortcutId
        this.tryNumber = tryNumber
        this.waitUntil = delayUntil?.toRealmInstant()
        this.waitForNetwork = waitForNetwork
        this.recursionDepth = recursionDepth
        this.resolvedVariables = resolvedVariables
        this.requestCode = requestCode
        scheduleType = type.name
    }

    @PrimaryKey
    var id: ExecutionId = ""
    var shortcutId: ShortcutId = ""

    @Suppress("unused")
    @Index
    var enqueuedAt: RealmInstant = RealmInstant.now()
    var tryNumber: Int = 0
    private var waitUntil: RealmInstant? = null
    val delayUntil: Instant?
        get() = waitUntil?.toInstant()

    var waitForNetwork: Boolean = false
    var recursionDepth: Int = 0
    var resolvedVariables: RealmList<ResolvedVariable> = realmListOf()
    var requestCode: Int = 0

    private var scheduleType: String = PendingExecutionType.UNKNOWN.name

    var type: PendingExecutionType
        get() = PendingExecutionType.parse(scheduleType)
        set(value) {
            scheduleType = value.name
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
            delayUntil: Instant? = null,
            waitForNetwork: Boolean = false,
            recursionDepth: Int = 0,
            type: PendingExecutionType,
            requestCode: Int,
        ): PendingExecution {
            val resolvedVariableList = realmListOf<ResolvedVariable>()
            resolvedVariables.mapTo(resolvedVariableList) {
                ResolvedVariable(it.key, it.value)
            }

            return PendingExecution(
                id = UUIDUtils.newUUID(),
                resolvedVariables = resolvedVariableList,
                shortcutId = shortcutId,
                tryNumber = tryNumber,
                delayUntil = delayUntil,
                waitForNetwork = waitForNetwork,
                recursionDepth = recursionDepth,
                type = type,
                requestCode = requestCode,
            )
        }
    }
}
