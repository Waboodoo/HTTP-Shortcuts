package ch.rmy.android.http_shortcuts.realm.models

import io.realm.RealmObject

open class ResolvedVariable : RealmObject() {

    var key: String? = null
    var value: String? = null

    companion object {

        fun createNew(key: String, value: String): ResolvedVariable {
            val resolvedVariable = ResolvedVariable()
            resolvedVariable.key = key
            resolvedVariable.value = value
            return resolvedVariable
        }
    }
}
