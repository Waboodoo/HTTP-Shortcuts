package ch.rmy.android.http_shortcuts.realm.models

import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePair
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
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

        fun createNew(key: String, value: String): Parameter {
            val parameter = Parameter()
            parameter.id = UUIDUtils.create()
            parameter.key = key
            parameter.value = value
            return parameter
        }
    }

    fun isSameAs(other: Parameter) = other.key == key && other.value == value

}
