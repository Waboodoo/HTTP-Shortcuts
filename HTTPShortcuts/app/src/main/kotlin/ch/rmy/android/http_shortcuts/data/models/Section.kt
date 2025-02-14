package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Section() : RealmObject {

    constructor(
        id: String = newUUID(),
        name: String = "",
    ) : this() {
        this.id = id
        this.name = name
    }

    @PrimaryKey
    var id: SectionId = newUUID()
    var name: String = ""

    fun validate() {
        require(name.isNotBlank()) { "Section without a name found" }
    }
}
