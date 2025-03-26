package ch.rmy.android.framework.extensions

import androidx.annotation.IntRange
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.framework.utils.localization.StaticLocalizable
import java.nio.charset.Charset

fun String.truncate(@IntRange(from = 1) maxLength: Int) =
    if (length > maxLength) substring(0, maxLength - 1) + "…" else this

fun String.replacePrefix(oldPrefix: String, newPrefix: String) =
    runIf(startsWith(oldPrefix)) {
        "$newPrefix${removePrefix(oldPrefix)}"
    }

fun <T : CharSequence> T.takeUnlessEmpty(): T? =
    takeUnless { it.isEmpty() }

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toChunkedHexString() =
    toHexString(HexFormat.UpperCase)
        .chunked(2)
        .joinToString(":")

fun String.fromHexString(): ByteArray =
    chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()

fun String.toLocalizable() =
    StaticLocalizable(this)

fun String.isUUID() =
    UUIDUtils.isUUID(this)

fun String.isInt() =
    toIntOrNull() != null

fun String.toCharset(): Charset? =
    try {
        Charset.forName(this)
    } catch (_: Exception) {
        null
    }
