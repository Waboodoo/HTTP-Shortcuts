package ch.rmy.android.http_shortcuts.activities.icons.models

import androidx.compose.runtime.Immutable

@Immutable
data class MaterialIcon(
    val name: String,
    val url: String,
    val keywords: Set<String>,
)
