package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.hasDuplicatesBy
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass(name = "Base")
open class BaseModel : RealmModel {

    var version: Long = 4
    var categories: RealmList<CategoryModel> = RealmList()
        private set
    var variables: RealmList<VariableModel> = RealmList()
        private set
    var title: String? = null
    var globalCode: String? = null

    val shortcuts: List<ShortcutModel>
        get() = categories.flatMap { it.shortcuts }

    fun validate() {
        categories.forEach(CategoryModel::validate)
        variables.forEach(VariableModel::validate)
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
