package ch.rmy.android.http_shortcuts.realm.models

import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
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

        fun createNew(label: String, value: String) =
            Option().apply {
                this.id = newUUID()
                this.label = label
                this.value = value
            }

    }

    fun isSameAs(other: Option) = other.label == label && other.value == value

}
