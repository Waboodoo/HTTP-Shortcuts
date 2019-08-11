package ch.rmy.android.http_shortcuts.extensions

fun String.truncate(maxLength: Int) =
    if (length > maxLength) substring(0, maxLength - 1) + "…" else this