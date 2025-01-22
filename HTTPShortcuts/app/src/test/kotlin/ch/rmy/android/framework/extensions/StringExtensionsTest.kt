package ch.rmy.android.framework.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StringExtensionsTest {

    @Test
    fun `short string remains unchanged when truncating`() {
        val string = "Hello World"
        val result = string.truncate(maxLength = 11)
        assertEquals(string, result)
    }

    @Test
    fun `long string is truncated`() {
        val string = "Hello World"
        val result = string.truncate(maxLength = 10)
        assertEquals("Hello Wor…", result)
    }

    @Test
    fun `string remains unchanged when no prefix exists to be replaced`() {
        val string = "Hello World"
        val result = string.replacePrefix(oldPrefix = "Hey", newPrefix = "Bye")
        assertEquals(string, result)
    }

    @Test
    fun `string prefix is replaced`() {
        val string = "Hello World"
        val result = string.replacePrefix(oldPrefix = "Hello", newPrefix = "Bye")
        assertEquals("Bye World", result)
    }

    @Test
    fun `non-empty string remains unchanged`() {
        val string = "Hello World"
        val result = string.takeUnlessEmpty()
        assertEquals(string, result)
    }

    @Test
    fun `empty string return null`() {
        val string = ""
        val result = string.takeUnlessEmpty()
        assertNull(result)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `byte array to hex string`() {
        val bytes = byteArrayOf(0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64)
        val result = bytes.toHexString()
        assertEquals(
            "48656c6c6f20576f726c64",
            result,
        )
    }
}
