package ch.rmy.android.http_shortcuts.realm.models

import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Option : RealmObject() {

    @PrimaryKey
    @Required
    var id: String = ""

    @Required
    var label: String = ""
    @Required
    var value: String = ""

    companion object {

        fun createNew(label: String, value: String): Option {
            val option = Option()
            option.id = UUIDUtils.create()
            option.label = label
            option.value = value
            return option
        }
    }

    fun isSameAs(other: Option) = other.label == label && other.value == value

}
