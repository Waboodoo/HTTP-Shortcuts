package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId

abstract class MainEvent : ViewModelEvent() {
    data class Restart(val activeCategoryId: CategoryId) : MainEvent()
}
