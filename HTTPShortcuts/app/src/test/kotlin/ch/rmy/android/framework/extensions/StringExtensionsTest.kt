package ch.rmy.android.framework.extensions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class StringExtensionsTest {

    @Test
    fun `short string remains unchanged when truncating`() {
        val string = "Hello World"
        val result = string.truncate(maxLength = 11)
        assertThat(result, equalTo(string))
    }

    @Test
    fun `long string is truncated`() {
        val string = "Hello World"
        val result = string.truncate(maxLength = 10)
        assertThat(result, equalTo("Hello Wor…"))
    }

    @Test
    fun `string remains unchanged when no prefix exists to be replaced`() {
        val string = "Hello World"
        val result = string.replacePrefix(oldPrefix = "Hey", newPrefix = "Bye")
        assertThat(result, equalTo(string))
    }

    @Test
    fun `string prefix is replaced`() {
        val string = "Hello World"
        val result = string.replacePrefix(oldPrefix = "Hello", newPrefix = "Bye")
        assertThat(result, equalTo("Bye World"))
    }

    @Test
    fun `non-empty string remains unchanged`() {
        val string = "Hello World"
        val result = string.takeUnlessEmpty()
        assertThat(result, equalTo(string))
    }

    @Test
    fun `empty string return null`() {
        val string = ""
        val result = string.takeUnlessEmpty()
        assertThat(result, equalTo(null))
    }

    @Test
    fun `byte array to hex string`() {
        val bytes = byteArrayOf(0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64)
        val result = bytes.toHexString()
        assertThat(result, equalTo("48656c6c6f20576f726c64"))
    }
}
