package ch.rmy.android.http_shortcuts.data

import android.content.Context
import ch.rmy.android.framework.utils.PreferencesStore
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import javax.inject.Inject

class SessionInfoStore
@Inject
constructor(
    context: Context,
) : PreferencesStore(context, PREF_NAME) {

    var editingShortcutId: ShortcutId?
        get() = getString(KEY_EDITING_SHORTCUT_ID)
        set(value) {
            putString(KEY_EDITING_SHORTCUT_ID, value)
        }

    var editingShortcutCategoryId: CategoryId?
        get() = getString(KEY_EDITING_SHORTCUT_CATEGORY_ID)
        set(value) {
            putString(KEY_EDITING_SHORTCUT_CATEGORY_ID, value)
        }

    companion object {
        private const val PREF_NAME = "session-info"

        private const val KEY_EDITING_SHORTCUT_ID = "editing_shortcut_id"
        private const val KEY_EDITING_SHORTCUT_CATEGORY_ID = "editing_shortcut_category_id"
    }
}
