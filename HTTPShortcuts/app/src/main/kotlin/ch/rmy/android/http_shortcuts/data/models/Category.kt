package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Category(
    @Required
    var name: String = ""
) : RealmObject(), HasId {

    @PrimaryKey
    override var id: String = ""
    var shortcuts: RealmList<Shortcut> = RealmList()
    @Required
    var layoutType: String = LAYOUT_LINEAR_LIST

    var background: String = BACKGROUND_TYPE_WHITE
    var hidden: Boolean = false

    companion object {

        const val LAYOUT_LINEAR_LIST = "linear_list"
        const val LAYOUT_GRID = "grid"

        const val BACKGROUND_TYPE_WHITE = "white"
        const val BACKGROUND_TYPE_BLACK = "black"
        const val BACKGROUND_TYPE_WALLPAPER = "wallpaper"

    }

}
