package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmList
import io.realm.RealmObject

open class Base : RealmObject() {

    var version: Long = 4
    var categories: RealmList<Category> = RealmList()
    var variables: RealmList<Variable> = RealmList()
    var title: String? = null
    var globalCode: String? = null

    val shortcuts: List<Shortcut>
        get() = categories.flatMap { it.shortcuts }

    fun validate() {
        categories.forEach(Category::validate)
        variables.forEach(Variable::validate)
    }
}
