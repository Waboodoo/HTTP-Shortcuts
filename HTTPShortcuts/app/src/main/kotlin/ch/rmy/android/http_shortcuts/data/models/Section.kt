package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId

@Entity(tableName = "section")
data class Section(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: SectionId,
    @ColumnInfo(name = "category_id", index = true)
    val categoryId: CategoryId,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "sorting_order", index = true)
    val sortingOrder: Int = 0,
)
