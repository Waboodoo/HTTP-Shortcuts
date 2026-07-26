package ch.rmy.android.http_shortcuts.shell_apk

import kotlin.test.Test
import kotlin.test.assertEquals

class ShellApkPackageNameFactoryTest {

    private val factory = ShellApkPackageNameFactory()

    @Test
    fun `uses first eight normalized shortcut id characters`() {
        assertEquals(
            "ch.rmy.android.http_shortcuts.app_abcdef12",
            factory.createPackageName(
                shortcutId = "ABCDEF12-3456-7890",
                allShortcutIds = listOf("ABCDEF12-3456-7890"),
            ),
        )
    }

    @Test
    fun `uses first four and last four normalized shortcut id characters when first eight collide`() {
        assertEquals(
            "ch.rmy.android.http_shortcuts.app_abcd9999",
            factory.createPackageName(
                shortcutId = "abcd1111-2222-9999",
                allShortcutIds = listOf(
                    "abcd1111-2222-9999",
                    "abcd1111-3333-8888",
                ),
            ),
        )
    }

    @Test
    fun `adds deterministic suffix when fallback also collides`() {
        val packageName = factory.createPackageName(
            shortcutId = "abcd1111-2222-9999",
            allShortcutIds = listOf(
                "abcd1111-2222-9999",
                "abcd1111-3333-8888",
                "abcd9999-4444-9999",
            ),
        )

        assertEquals(
            true,
            packageName.startsWith("ch.rmy.android.http_shortcuts.app_abcd9999_"),
        )
    }

    @Test
    fun `normalizes invalid characters out of package suffix`() {
        assertEquals(
            "ch.rmy.android.http_shortcuts.app_shortcut",
            factory.createPackageName(
                shortcutId = "Shortcut ID!",
                allShortcutIds = listOf("Shortcut ID!"),
            ),
        )
    }
}
