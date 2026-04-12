package ch.rmy.iconfetcher

import kotlin.time.Duration.Companion.days

internal object Config {
    const val ICONS_FILE = "meta.json"
    val CACHE_MAX_AGE = 7.days
}
