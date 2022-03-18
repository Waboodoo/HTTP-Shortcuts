package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(name = "Header")
open class HeaderModel(
    @PrimaryKey
    @Required
    var id: String = newUUID(),
    @Required
    var key: String = "",
    @Required
    var value: String = "",
) : RealmObject() {

    fun isSameAs(other: HeaderModel) = other.key == key && other.value == value
}
