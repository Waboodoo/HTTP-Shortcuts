package ch.rmy.android.http_shortcuts.data.realm.models

import ch.rmy.android.http_shortcuts.data.models.Shortcut
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
class Widget() : RealmObject {
    @PrimaryKey
    var widgetId: Int = 0
    var shortcut: Shortcut? = null
    var labelColor: String? = null
    var showLabel: Boolean = true
    var showIcon: Boolean = true
    var iconScale: Float = 1f
}
