package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Header() : RealmObject {

    constructor(
        id: String = newUUID(),
        key: String = "",
        value: String = "",
    ) : this() {
        this.id = id
        this.key = key
        this.value = value
    }

    @PrimaryKey
    var id: String = newUUID()
    var key: String = ""
    var value: String = ""

    fun isSameAs(other: Header) =
        other.key == key && other.value == value

    fun validate() {
        require(id.isUUID()) {
            "Invalid header ID found, must be UUID: $id"
        }
        require(key.isNotEmpty()) {
            "Header without a key found"
        }
    }
}
