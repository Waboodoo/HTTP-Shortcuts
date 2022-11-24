package ch.rmy.android.http_shortcuts.test

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.models.BaseModel
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.data.models.HeaderModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel

fun createBase(categories: List<CategoryModel>): BaseModel {
    val base = BaseModel()
    base.categories.addAll(categories)
    return base
}

fun createCategory(id: String? = null, shortcuts: List<ShortcutModel> = emptyList()): CategoryModel {
    val category = CategoryModel()
    category.id = id ?: UUIDUtils.newUUID()
    category.name = "Shortcuts"
    category.shortcuts.addAll(shortcuts)
    return category
}

fun createShortcut(id: String? = null, headers: List<HeaderModel> = emptyList()): ShortcutModel {
    val shortcut = ShortcutModel()
    shortcut.id = id ?: UUIDUtils.newUUID()
    shortcut.name = "Shortcut"
    shortcut.headers.addAll(headers)
    return shortcut
}

fun createHeader(id: String? = null): HeaderModel {
    val header = HeaderModel()
    header.id = id ?: UUIDUtils.newUUID()
    header.key = "Header"
    return header
}
