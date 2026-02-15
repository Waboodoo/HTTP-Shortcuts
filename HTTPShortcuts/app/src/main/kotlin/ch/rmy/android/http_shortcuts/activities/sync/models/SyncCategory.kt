package ch.rmy.android.http_shortcuts.activities.sync.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId

@Stable
data class SyncCategory(
    val id: CategoryId,
    val name: String,
    val checked: Boolean,
)
