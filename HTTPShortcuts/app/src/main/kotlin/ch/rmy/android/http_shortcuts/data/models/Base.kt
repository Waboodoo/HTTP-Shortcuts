package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmList
import io.realm.RealmObject

open class Base : RealmObject() {

    var version: Long = 4
    var categories: RealmList<Category> = RealmList()
        private set
    var variables: RealmList<Variable> = RealmList()
        private set
    var title: String? = null
    var globalCode: String? = null

    val shortcuts: List<Shortcut>
        get() = categories.flatMap { it.shortcuts }

    fun validate() {
        categories.forEach(Category::validate)
        variables.forEach(Variable::validate)
        if (hasDuplicateVariableKeys()) {
            throw IllegalArgumentException("Duplicate variable keys")
        }
    }

    private fun hasDuplicateVariableKeys(): Boolean {
        val keys = variables.map { it.key }
        return keys.distinct().size != keys.size
    }
}
