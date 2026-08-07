package ch.rmy.android.http_shortcuts.shell_apk

import kotlin.test.Test
import kotlin.test.assertEquals

class ShellApkPackageNameFactoryTest {

    private val factory = ShellApkPackageNameFactory()

    @Test
    fun `uses the complete normalized UUID shortcut id`() {
        assertEquals(
            "ch.rmy.android.http_shortcuts.app_abcdef1234567890",
            factory.createPackageName(
                shortcutId = "ABCDEF12-3456-7890",
            ),
        )
    }

    @Test
    fun `uses a positive integer shortcut id`() {
        assertEquals(
            "ch.rmy.android.http_shortcuts.app_12345",
            factory.createPackageName(
                shortcutId = "12345",
            ),
        )
    }
}
