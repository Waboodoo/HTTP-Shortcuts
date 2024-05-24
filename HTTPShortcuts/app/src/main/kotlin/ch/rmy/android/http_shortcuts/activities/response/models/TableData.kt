package ch.rmy.android.http_shortcuts.activities.response.models

import androidx.compose.runtime.Stable

@Stable
data class TableData(
    val columns: List<String>,
    val rows: List<Map<String, String>>,
)
