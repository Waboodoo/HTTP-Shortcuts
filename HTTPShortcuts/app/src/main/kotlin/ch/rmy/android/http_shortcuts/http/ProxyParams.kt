package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.data.enums.ProxyType

data class ProxyParams(
    val type: ProxyType,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
)
