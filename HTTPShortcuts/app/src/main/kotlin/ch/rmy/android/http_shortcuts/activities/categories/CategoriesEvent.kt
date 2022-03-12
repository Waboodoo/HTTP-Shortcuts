package ch.rmy.android.http_shortcuts.activities.categories

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class CategoriesEvent : ViewModelEvent() {
    object RequestFilePermissionsIfNeeded : CategoriesEvent()
}
