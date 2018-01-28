package ch.rmy.android.http_shortcuts.realm.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Category : RealmObject(), HasId {

    @PrimaryKey
    override var id: Long = 0
    @Required
    var name: String = ""
    var shortcuts: RealmList<Shortcut> = RealmList()
    @Required
    var layoutType: String = LAYOUT_LINEAR_LIST

    override val isNew: Boolean
        get() = id == 0L

    companion object {

        const val LAYOUT_LINEAR_LIST = "linear_list"
        const val LAYOUT_GRID = "grid"

        fun createNew(name: String): Category {
            val category = Category()
            category.id = 0
            category.name = name
            category.shortcuts = RealmList<Shortcut>()
            category.layoutType = LAYOUT_LINEAR_LIST
            return category
        }
    }

}
