package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.isUUID
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(name = "Category")
open class CategoryModel(
    @Required
    var name: String = "",
) : RealmObject() {

    @PrimaryKey
    var id: String = ""
    var shortcuts: RealmList<ShortcutModel> = RealmList()

    @Required
    private var layoutType: String = CategoryLayoutType.LINEAR_LIST.type

    @Required
    private var background: String = CategoryBackgroundType.WHITE.type
    var hidden: Boolean = false

    var categoryLayoutType
        get() = CategoryLayoutType.parse(layoutType)
        set(value) {
            layoutType = value.type
        }

    var categoryBackgroundType
        get() = CategoryBackgroundType.parse(background)
        set(value) {
            background = value.type
        }

    fun validate() {
        if (!isUUID(id) && id.toIntOrNull() == null) {
            throw IllegalArgumentException("Invalid category ID found, must be UUID: $id")
        }

        if (CategoryLayoutType.values().none { it.type == layoutType }) {
            throw IllegalArgumentException("Invalid layout type: $layoutType")
        }

        if (CategoryBackgroundType.values().none { it.type == background }) {
            throw IllegalArgumentException("Invalid background: $background")
        }

        shortcuts.forEach(ShortcutModel::validate)
    }

    companion object {
        const val FIELD_ID = "id"
    }
}
