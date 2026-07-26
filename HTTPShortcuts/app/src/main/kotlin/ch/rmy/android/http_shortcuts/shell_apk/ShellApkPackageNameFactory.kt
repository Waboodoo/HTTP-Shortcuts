package ch.rmy.android.http_shortcuts.shell_apk

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import java.util.Locale
import java.util.zip.CRC32
import javax.inject.Inject

class ShellApkPackageNameFactory
@Inject
constructor() {

    fun createPackageName(shortcutId: ShortcutId, allShortcutIds: Collection<ShortcutId>): String {
        val normalizedId = shortcutId.normalizedForPackageName()
        val candidate = normalizedId.take(8)
        if (!candidate.hasCollision(shortcutId, allShortcutIds)) {
            return "$PACKAGE_PREFIX$candidate"
        }

        val fallback = normalizedId.take(4) + normalizedId.takeLast(4)
        if (!fallback.hasCollision(shortcutId, allShortcutIds)) {
            return "$PACKAGE_PREFIX$fallback"
        }

        return "$PACKAGE_PREFIX${fallback}_${shortcutId.crc32Suffix()}"
    }

    private fun String.hasCollision(shortcutId: ShortcutId, allShortcutIds: Collection<ShortcutId>): Boolean =
        allShortcutIds
            .asSequence()
            .filter { it != shortcutId }
            .map { it.normalizedForPackageName() }
            .any { otherId ->
                otherId.take(length) == this ||
                    (length == 8 && otherId.take(4) + otherId.takeLast(4) == this)
            }

    private fun ShortcutId.normalizedForPackageName(): String =
        lowercase(Locale.US)
            .filter { it in 'a'..'z' || it in '0'..'9' || it == '_' }
            .takeIf { it.isNotEmpty() }
            ?: crc32Suffix()

    private fun ShortcutId.crc32Suffix(): String {
        val crc = CRC32()
        crc.update(toByteArray())
        return crc.value.toString(16).padStart(8, '0').takeLast(8)
    }

    companion object {
        const val PACKAGE_PREFIX = "ch.rmy.android.http_shortcuts.app_"
    }
}
