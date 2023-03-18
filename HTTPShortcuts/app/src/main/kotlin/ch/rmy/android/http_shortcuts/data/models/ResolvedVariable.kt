package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ResolvedVariable() : RealmObject {

    constructor(
        key: VariableKey = "",
        value: String = "",
    ) : this() {
        this.key = key
        this.value = value
    }

    @PrimaryKey
    var id: String = newUUID()
    var key: VariableKey = ""
    var value: String = ""
}
