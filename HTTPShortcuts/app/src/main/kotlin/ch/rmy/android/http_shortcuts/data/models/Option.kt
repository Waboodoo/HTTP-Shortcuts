package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Option() : RealmObject {

    constructor(
        id: String = newUUID(),
        label: String = "",
        value: String = "",
    ) : this() {
        this.id = id
        this.label = label
        this.value = value
    }

    @PrimaryKey
    var id: String = newUUID()
    var label: String = ""
    var value: String = ""

    val labelOrValue: String
        get() = label
            .ifEmpty { value }
            .ifEmpty { "-" }

    fun isSameAs(other: Option) =
        other.label == label && other.value == value

    fun validate() {
        require(id.isUUID()) {
            "Invalid option ID found, must be UUID: $id"
        }
    }
}
