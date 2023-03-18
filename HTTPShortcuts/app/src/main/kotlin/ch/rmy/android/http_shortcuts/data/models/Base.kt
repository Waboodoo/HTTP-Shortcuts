package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.hasDuplicatesBy
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

class Base : RealmObject {

    var version: Long = 4
    var categories: RealmList<Category> = realmListOf()
        private set
    var variables: RealmList<Variable> = realmListOf()
        private set
    var title: String? = null
    var globalCode: String? = null

    val shortcuts: List<Shortcut>
        get() = categories.flatMap { it.shortcuts }

    fun validate() {
        categories.forEach(Category::validate)
        variables.forEach(Variable::validate)
        require(!categories.hasDuplicatesBy { it.id }) {
            "Duplicate category IDs"
        }
        require(!variables.hasDuplicatesBy { it.id }) {
            "Duplicate variable IDs"
        }
        require(!variables.flatMap { it.options ?: emptyList() }.hasDuplicatesBy { it.id }) {
            "Duplicate variable option IDs"
        }
        require(!variables.hasDuplicatesBy { it.key }) {
            "Duplicate variable keys"
        }
        require(!shortcuts.hasDuplicatesBy { it.id }) {
            "Duplicate shortcut IDs"
        }
        require(!shortcuts.flatMap { it.headers }.hasDuplicatesBy { it.id }) {
            "Duplicate header IDs"
        }
        require(!shortcuts.flatMap { it.parameters }.hasDuplicatesBy { it.id }) {
            "Duplicate parameter IDs"
        }
    }
}
