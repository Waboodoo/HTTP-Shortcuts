package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.isUUID
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(name = "Category")
open class CategoryModel(
    @Required
    var name: String = "",
    categoryLayoutType: CategoryLayoutType = CategoryLayoutType.LINEAR_LIST,
    categoryBackgroundType: CategoryBackgroundType = CategoryBackgroundType.Default,
    clickBehavior: ShortcutClickBehavior? = null,
) : RealmObject() {

    @PrimaryKey
    var id: CategoryId = ""
    var shortcuts: RealmList<ShortcutModel> = RealmList()

    @Required
    private var layoutType: String = CategoryLayoutType.LINEAR_LIST.type

    @Required
    private var background: String = CategoryBackgroundType.Default.serialize()
    var hidden: Boolean = false

    private var shortcutClickBehavior: String? = null

    var categoryLayoutType
        get() = CategoryLayoutType.parse(layoutType)
        set(value) {
            layoutType = value.type
        }

    var categoryBackgroundType
        get() = CategoryBackgroundType.parse(background)
        set(value) {
            background = value.serialize()
        }

    var clickBehavior: ShortcutClickBehavior?
        get() = shortcutClickBehavior?.let(ShortcutClickBehavior::parse)
        set(value) {
            shortcutClickBehavior = value?.type
        }

    init {
        layoutType = categoryLayoutType.type
        background = categoryBackgroundType.serialize()
        shortcutClickBehavior = clickBehavior?.type
    }

    fun validate() {
        if (!isUUID(id) && id.toIntOrNull() == null) {
            throw IllegalArgumentException("Invalid category ID found, must be UUID: $id")
        }

        if (CategoryLayoutType.values().none { it.type == layoutType || it.legacyAlias == layoutType }) {
            throw IllegalArgumentException("Invalid layout type: $layoutType")
        }

        if (name.isBlank()) {
            throw IllegalArgumentException("Category without a name found")
        }

        if (name.length > NAME_MAX_LENGTH) {
            throw IllegalArgumentException("Category name too long: $name")
        }

        shortcuts.forEach(ShortcutModel::validate)
    }

    companion object {
        const val FIELD_ID = "id"

        const val NAME_MAX_LENGTH = 50
    }
}
