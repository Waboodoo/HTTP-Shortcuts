package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Option(
    @PrimaryKey
    @Required
    var id: String = newUUID(),
    @Required
    var label: String = "",
    @Required
    var value: String = ""

) : RealmObject() {

    val labelOrValue: String
        get() = label
            .ifEmpty { value }
            .ifEmpty { "-" }

    fun isSameAs(other: Option) = other.label == label && other.value == value

}
