package ch.rmy.iconfetcher

import kotlin.time.Duration.Companion.days

internal object Config {
    const val ICONS_BASE_URL = "https://http-shortcuts.rmy.ch/material-icons/"
    const val ICONS_FILE = "icons.json"
    val CACHE_MAX_AGE = 7.days
}
