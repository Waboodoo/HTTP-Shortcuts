package ch.rmy.android.http_shortcuts.data.settings

import android.content.Context
import ch.rmy.android.framework.utils.PreferencesStore
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import javax.inject.Inject

class SyncSettings
@Inject
constructor(
    context: Context,
) : PreferencesStore(context, PREF_NAME) {
    var syncType: SyncType?
        get() = getString(KEY_SYNC_TYPE)?.let { SyncType.parse(it) }
        set(value) {
            putString(KEY_SYNC_TYPE, value?.name)
        }

    companion object {
        private const val PREF_NAME = "sync"
        private const val KEY_SYNC_TYPE = "sync_type"
    }
}
