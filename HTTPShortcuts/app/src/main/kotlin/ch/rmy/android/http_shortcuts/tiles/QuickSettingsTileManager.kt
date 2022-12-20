package ch.rmy.android.http_shortcuts.tiles

import android.os.Build
import javax.inject.Inject

class QuickSettingsTileManager
@Inject
constructor() {

    fun supportsQuickSettingsTiles(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}
