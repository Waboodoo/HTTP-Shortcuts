package ch.rmy.android.http_shortcuts.data.models

import androidx.annotation.Keep
import ch.rmy.android.framework.extensions.hasDuplicatesBy
import ch.rmy.android.http_shortcuts.data.realm.models.Variable as VariableRealmModel
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

class Base : RealmObject {
    @Deprecated("Only used for backwards compatibility")
    @Keep
    var version: Long = 0
    @Deprecated("Only used for backwards compatibility")
    @Keep
    var compatibilityVersion: Long = 0
    var categories: RealmList<Category> = realmListOf()
        private set

    @Deprecated("Only used in Realm-to-Room migration")
    var variables: RealmList<VariableRealmModel> = realmListOf()
        private set

    val shortcuts: List<Shortcut>
        get() = categories.flatMap { it.shortcuts }

    @Deprecated("Only used in Realm-to-Room migration")
    var title: String? = null

    @Deprecated("Only used in Realm-to-Room migration")
    var globalCode: String? = null

    fun validate() {
        categories.forEach(Category::validate)
        require(!categories.hasDuplicatesBy { it.id }) {
            "Duplicate category IDs"
        }
        require(!shortcuts.hasDuplicatesBy { it.id }) {
            "Duplicate shortcut IDs"
        }
        require(!shortcuts.flatMap { it.headers }.hasDuplicatesBy { it.id }) {
            "Duplicate header IDs"
        }
        require(!shortcuts.flatMap { it.parameters }.hasDuplicatesBy { it.id }) {
            "Duplicate parameter IDs"
        }
    }
}
