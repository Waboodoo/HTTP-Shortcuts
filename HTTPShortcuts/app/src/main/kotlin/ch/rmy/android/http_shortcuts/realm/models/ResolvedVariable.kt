package ch.rmy.android.http_shortcuts.realm.models

import io.realm.RealmObject
import io.realm.annotations.Required

open class ResolvedVariable : RealmObject() {

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
