package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(name = "Base")
open class BaseModel : RealmObject() {

    var version: Long = 4
    var categories: RealmList<CategoryModel> = RealmList()
        private set
    var variables: RealmList<VariableModel> = RealmList()
        private set
    var title: String? = null
    var globalCode: String? = null

    val shortcuts: List<ShortcutModel>
        get() = categories.flatMap { it.shortcuts }

    var pollingShortcuts: RealmList<ShortcutModel> = RealmList()

    fun validate() {
        categories.forEach(CategoryModel::validate)
        variables.forEach(VariableModel::validate)
        if (hasDuplicateVariableKeys()) {
            throw IllegalArgumentException("Duplicate variable keys")
        }
    }

    private fun hasDuplicateVariableKeys(): Boolean {
        val keys = variables.map { it.key }
        return keys.distinct().size != keys.size
    }
}
