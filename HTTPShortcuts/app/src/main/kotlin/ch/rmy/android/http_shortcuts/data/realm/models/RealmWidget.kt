package ch.rmy.android.http_shortcuts.data.realm.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "Widget")
class RealmWidget() : RealmObject {
    @PrimaryKey
    var widgetId: Int = 0
    var shortcut: RealmShortcut? = null
    var labelColor: String? = null
    var showLabel: Boolean = true
    var showIcon: Boolean = true
    var iconScale: Float = 1f
}
