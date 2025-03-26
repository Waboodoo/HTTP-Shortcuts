package ch.rmy.android.http_shortcuts.data.realm.models

import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "Category")
class RealmCategory() : RealmObject {
    @PrimaryKey
    var id: CategoryId = ""
    var name: String = ""
    var shortcuts: RealmList<RealmShortcut> = realmListOf()
    var sections: RealmList<RealmSection> = realmListOf()
    var iconName: String? = null
    var layoutType: String = CategoryLayoutType.LINEAR_LIST.type
    var background: String = CategoryBackgroundType.Default.serialize()
    var hidden: Boolean = false
    var shortcutClickBehavior: String? = null
}
