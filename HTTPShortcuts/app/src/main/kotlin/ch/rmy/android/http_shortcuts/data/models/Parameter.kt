package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Parameter() : RealmObject {

    constructor(
        id: String = newUUID(),
        key: String = "",
        value: String = "",
        parameterType: ParameterType = ParameterType.STRING,
        fileName: String = "",
        fileUploadOptions: FileUploadOptions? = null,
    ) : this() {
        this.id = id
        this.key = key
        this.value = value
        this.fileName = fileName
        this.fileUploadOptions = fileUploadOptions
        type = parameterType.type
    }

    @PrimaryKey
    var id: String = newUUID()
    var key: String = ""
    var value: String = ""
    var fileName: String = ""

    private var type: String = ParameterType.STRING.type

    var parameterType: ParameterType
        get() = ParameterType.parse(type)
        set(value) {
            type = value.type
        }

    var fileUploadOptions: FileUploadOptions? = null

    fun isSameAs(other: Parameter) =
        other.key == key &&
            other.value == value &&
            other.type == type &&
            other.fileName == fileName &&
            other.fileUploadOptions?.isSameAs(fileUploadOptions) == true

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
