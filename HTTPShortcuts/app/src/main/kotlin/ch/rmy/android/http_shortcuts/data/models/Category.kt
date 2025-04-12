package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Entity(tableName = "category")
data class Category(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: CategoryId,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "icon")
    val icon: ShortcutIcon?,
    @ColumnInfo(name = "layout_type")
    val layoutType: CategoryLayoutType,
    @ColumnInfo(name = "background")
    val background: CategoryBackgroundType,
    @ColumnInfo(name = "hidden")
    val hidden: Boolean,
    @ColumnInfo(name = "scale")
    val scale: Float,
    @ColumnInfo(name = "shortcut_click_behavior")
    val shortcutClickBehavior: ShortcutClickBehavior?,
    @ColumnInfo(name = "sorting_order", index = true)
    val sortingOrder: Int = 0,
)
