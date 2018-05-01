package ch.rmy.android.http_shortcuts.actions

import ch.rmy.android.http_shortcuts.utils.UUIDUtils

data class ActionDTO(
        val id: String = UUIDUtils.create(),
        val type: String,
        val data: Map<String, String> = emptyMap()
)
