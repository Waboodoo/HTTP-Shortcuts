package ch.rmy.android.http_shortcuts.data.domains

import ch.rmy.android.framework.data.RealmContext
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import io.realm.kotlin.query.RealmQuery

fun RealmContext.getBase(): RealmQuery<Base> =
    get()

fun RealmContext.getCategoryById(categoryId: CategoryId): RealmQuery<Category> =
    get("${Category.FIELD_ID} == $0", categoryId)

fun RealmContext.getCategoryByNameOrId(categoryNameOrId: String): RealmQuery<Category> =
    get("${Category.FIELD_ID} == $0 OR ${Category.FIELD_NAME} ==[c] $1", categoryNameOrId, categoryNameOrId)

fun RealmContext.getShortcutById(shortcutId: ShortcutId): RealmQuery<Shortcut> =
    get("${Shortcut.FIELD_ID} == $0", shortcutId)

fun RealmContext.getTemporaryShortcut(): RealmQuery<Shortcut> =
    getShortcutById(Shortcut.TEMPORARY_ID)

fun RealmContext.getShortcutByNameOrId(shortcutNameOrId: ShortcutNameOrId): RealmQuery<Shortcut> =
    get("${Shortcut.FIELD_ID} == $0 OR ${Shortcut.FIELD_NAME} ==[c] $1", shortcutNameOrId, shortcutNameOrId)
