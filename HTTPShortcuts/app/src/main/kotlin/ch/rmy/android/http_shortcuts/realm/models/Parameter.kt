package ch.rmy.android.http_shortcuts.realm.models

import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePair
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Parameter : RealmObject(), KeyValuePair {

    @PrimaryKey
    var id: String? = null

    @Required
    override var key: String = ""
    @Required
    override var value: String = ""

    companion object {

        fun createNew(key: String, value: String) =
            Parameter().apply {
                this.id = newUUID()
                this.key = key
                this.value = value
            }
    }

    fun isSameAs(other: Parameter) = other.key == key && other.value == value

}
