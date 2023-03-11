package ch.rmy.android.http_shortcuts.test

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Shortcut

fun createBase(categories: List<Category>): Base {
    val base = Base()
    base.categories.addAll(categories)
    return base
}

fun createCategory(id: String? = null, shortcuts: List<Shortcut> = emptyList()): Category {
    val category = Category()
    category.id = id ?: UUIDUtils.newUUID()
    category.name = "Shortcuts"
    category.shortcuts.addAll(shortcuts)
    return category
}

fun createShortcut(id: String? = null, headers: List<Header> = emptyList()): Shortcut {
    val shortcut = Shortcut()
    shortcut.id = id ?: UUIDUtils.newUUID()
    shortcut.name = "Shortcut"
    shortcut.headers.addAll(headers)
    return shortcut
}

fun createHeader(id: String? = null): Header {
    val header = Header()
    header.id = id ?: UUIDUtils.newUUID()
    header.key = "Header"
    return header
}
