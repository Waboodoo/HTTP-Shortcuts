package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class ResolvedVariable : RealmObject() {
    @PrimaryKey
    @Required
    var id: String = newUUID()

    @Required
    var key: String = ""

    @Required
    var value: String = ""

    companion object {

        fun createNew(key: String, value: String): ResolvedVariable {
            val resolvedVariable = ResolvedVariable()
            resolvedVariable.key = key
            resolvedVariable.value = value
            return resolvedVariable
        }
    }
}
