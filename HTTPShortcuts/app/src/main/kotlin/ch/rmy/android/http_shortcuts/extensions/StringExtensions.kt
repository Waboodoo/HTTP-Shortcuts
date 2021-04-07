package ch.rmy.android.http_shortcuts.extensions

fun String.truncate(maxLength: Int) =
    if (length > maxLength) substring(0, maxLength - 1) + "…" else this

fun String.replacePrefix(oldPrefix: String, newPrefix: String) =
    mapIf(startsWith(oldPrefix)) {
        "$newPrefix${removePrefix(oldPrefix)}"
    }

fun String.takeUnlessEmpty() =
    takeUnless { it.isEmpty() }

fun ByteArray.toHexString() =
    joinToString("") { "%02x".format(it) }
