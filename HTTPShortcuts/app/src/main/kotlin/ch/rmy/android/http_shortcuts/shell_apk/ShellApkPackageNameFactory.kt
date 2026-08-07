package ch.rmy.android.http_shortcuts.shell_apk

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import java.util.Locale
import javax.inject.Inject

class ShellApkPackageNameFactory
@Inject
constructor() {

    fun createPackageName(shortcutId: ShortcutId): String =
        "$PACKAGE_PREFIX${shortcutId.normalizedForPackageName()}"

    private fun ShortcutId.normalizedForPackageName(): String =
        lowercase(Locale.US)
            .filter { it in 'a'..'z' || it in '0'..'9' }

    companion object {
        private const val PACKAGE_PREFIX = "ch.rmy.android.http_shortcuts.app_"
    }
}
