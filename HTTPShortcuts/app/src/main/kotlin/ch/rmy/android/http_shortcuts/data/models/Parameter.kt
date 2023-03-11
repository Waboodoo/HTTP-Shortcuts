package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass
open class Parameter(
    @PrimaryKey
    @Required
    var id: String = newUUID(),
    @Required
    var key: String = "",
    @Required
    var value: String = "",
    parameterType: ParameterType = ParameterType.STRING,
    var fileName: String = "",
) : RealmModel {

    @Required
    private var type: String = ParameterType.STRING.type

    var parameterType: ParameterType
        get() = ParameterType.parse(type)
        set(value) {
            type = value.type
        }

    init {
        type = parameterType.type
    }

    fun isSameAs(other: Parameter) =
        other.key == key &&
            other.value == value &&
            other.type == type &&
            other.fileName == fileName

    val isStringParameter: Boolean
        get() = type == ParameterType.STRING.type

    fun validate() {
        require(id.isUUID()) {
            "Invalid parameter ID found, must be UUID: $id"
        }
        require(key.isNotEmpty()) {
            "Parameter without a key found"
        }
    }
}
