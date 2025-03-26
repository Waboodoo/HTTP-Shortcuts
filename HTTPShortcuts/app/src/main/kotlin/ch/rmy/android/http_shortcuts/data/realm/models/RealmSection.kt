package ch.rmy.android.http_shortcuts.data.realm.models

import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "Section")
class RealmSection() : RealmObject {
    @PrimaryKey
    var id: SectionId = ""
    var name: String = ""
}
