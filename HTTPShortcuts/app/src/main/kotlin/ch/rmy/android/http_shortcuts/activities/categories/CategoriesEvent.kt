package ch.rmy.android.http_shortcuts.activities.categories

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId

abstract class CategoriesEvent : ViewModelEvent() {
    data class OpenCategoryEditor(val categoryId: CategoryId?) : CategoriesEvent()
    object OpenCustomIconPicker : CategoriesEvent()
}
