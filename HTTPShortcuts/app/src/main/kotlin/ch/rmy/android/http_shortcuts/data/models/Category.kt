package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Category() : RealmObject {

    constructor(
        name: String = "",
        categoryLayoutType: CategoryLayoutType = CategoryLayoutType.LINEAR_LIST,
        categoryBackgroundType: CategoryBackgroundType = CategoryBackgroundType.Default,
        clickBehavior: ShortcutClickBehavior? = null,
    ) : this() {
        this.name = name
        this.categoryLayoutType = categoryLayoutType
        this.categoryBackgroundType = categoryBackgroundType
        this.clickBehavior = clickBehavior
    }

    @PrimaryKey
    var id: CategoryId = ""
    var name: String = ""
    var shortcuts: RealmList<Shortcut> = realmListOf()

    private var iconName: String? = null

    var icon: ShortcutIcon?
        get() = iconName?.let(ShortcutIcon::fromName)
        set(value) {
            iconName = value?.toString()?.takeUnlessEmpty()
        }

    private var layoutType: String = CategoryLayoutType.LINEAR_LIST.type

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

    fun validate() {
        require(id.isUUID() || id.isInt()) {
            "Invalid category ID found, must be UUID: $id"
        }
        require(CategoryLayoutType.entries.any { it.type == layoutType || it.legacyAlias == layoutType }) {
            "Invalid layout type: $layoutType"
        }
        require(name.isNotBlank()) {
            "Category without a name found"
        }
        require(name.length <= NAME_MAX_LENGTH) {
            "Category name too long: $name"
        }
        shortcuts.forEach(Shortcut::validate)
    }

    companion object {
        const val FIELD_ID = "id"

        const val NAME_MAX_LENGTH = 50
    }
}
