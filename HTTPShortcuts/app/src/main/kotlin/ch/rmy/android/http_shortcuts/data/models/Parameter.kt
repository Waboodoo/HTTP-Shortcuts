package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Parameter(
    @PrimaryKey
    @Required
    override var id: String = newUUID(),
    @Required
    var key: String = "",
    @Required
    var value: String = ""
) : RealmObject(), HasId {

    fun isSameAs(other: Parameter) = other.key == key && other.value == value

}
