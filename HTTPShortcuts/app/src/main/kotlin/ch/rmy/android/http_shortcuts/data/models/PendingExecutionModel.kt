package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.Date

@RealmClass(name = "PendingExecution")
open class PendingExecutionModel(
    @PrimaryKey
    var id: String = "",
    var shortcutId: String = "",
    @Suppress("unused")
    @Index
    @Required
    var enqueuedAt: Date = Date(),
    var tryNumber: Int = 0,
    var waitUntil: Date? = null,
    @Suppress("unused")
    var waitForNetwork: Boolean = false,
    var recursionDepth: Int = 0,
    var resolvedVariables: RealmList<ResolvedVariableModel> = RealmList(),
) : RealmObject() {

    companion object {

        const val FIELD_ID = "id"
        const val FIELD_SHORTCUT_ID = "shortcutId"
        const val FIELD_ENQUEUED_AT = "enqueuedAt"
        const val FIELD_WAIT_FOR_NETWORK = "waitForNetwork"

        fun createNew(
            shortcutId: String,
            resolvedVariables: Map<String, String> = emptyMap(),
            tryNumber: Int = 0,
            waitUntil: Date? = null,
            waitForNetwork: Boolean = false,
            recursionDepth: Int = 0,
        ): PendingExecutionModel {
            val resolvedVariableList = RealmList<ResolvedVariableModel>()
            resolvedVariables.mapTo(resolvedVariableList) {
                ResolvedVariableModel(it.key, it.value)
            }

            return PendingExecutionModel(
                id = UUIDUtils.newUUID(),
                resolvedVariables = resolvedVariableList,
                shortcutId = shortcutId,
                tryNumber = tryNumber,
                waitUntil = waitUntil,
                enqueuedAt = Date(),
                waitForNetwork = waitForNetwork,
                recursionDepth = recursionDepth
            )
        }
    }
}
