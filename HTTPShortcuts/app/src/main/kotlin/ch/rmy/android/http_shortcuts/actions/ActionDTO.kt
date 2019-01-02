package ch.rmy.android.http_shortcuts.actions

import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID

data class ActionDTO(
    val id: String = newUUID(),
    val type: String,
    val data: Map<String, String> = emptyMap()
)
