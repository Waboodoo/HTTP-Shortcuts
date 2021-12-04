package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Category(
    @Required
    var name: String = "",
) : RealmObject(), HasId {

    @PrimaryKey
    override var id: String = ""
    var shortcuts: RealmList<Shortcut> = RealmList()

    @Required
    var layoutType: String = LAYOUT_LINEAR_LIST

    var background: String = BACKGROUND_TYPE_WHITE
    var hidden: Boolean = false

    fun validate() {
        if (!UUIDUtils.isUUID(id) && id.toIntOrNull() == null) {
            throw IllegalArgumentException("Invalid category ID found, must be UUID: $id")
        }

        if (layoutType !in setOf(LAYOUT_GRID, LAYOUT_LINEAR_LIST)) {
            throw IllegalArgumentException("Invalid layout type: $layoutType")
        }

        if (background !in setOf(BACKGROUND_TYPE_WHITE, BACKGROUND_TYPE_BLACK, BACKGROUND_TYPE_WALLPAPER)) {
            throw IllegalArgumentException("Invalid background: $background")
        }

        shortcuts.forEach(Shortcut::validate)
    }

    companion object {

        const val LAYOUT_LINEAR_LIST = "linear_list"
        const val LAYOUT_GRID = "grid"

        const val BACKGROUND_TYPE_WHITE = "white"
        const val BACKGROUND_TYPE_BLACK = "black"
        const val BACKGROUND_TYPE_WALLPAPER = "wallpaper"
    }
}
