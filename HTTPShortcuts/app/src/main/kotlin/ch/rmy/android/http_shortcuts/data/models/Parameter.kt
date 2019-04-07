package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePair
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Parameter(
    @PrimaryKey
    var id: String? = newUUID(),
    @Required
    override var key: String = "",
    @Required
    override var value: String = ""
) : RealmObject(), KeyValuePair {

    fun isSameAs(other: Parameter) = other.key == key && other.value == value

}
