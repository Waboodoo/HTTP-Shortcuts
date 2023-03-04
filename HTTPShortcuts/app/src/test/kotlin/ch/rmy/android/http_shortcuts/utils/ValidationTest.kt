package ch.rmy.android.http_shortcuts.utils

import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableHttpUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isValidHttpUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isValidUrl
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationTest {

    @Test
    fun testValidUrlAcceptable() {
        assertTrue(isAcceptableHttpUrl("http://example.com"))
        assertTrue(isAcceptableHttpUrl("https://example.com"))
        assertTrue(isAcceptableHttpUrl("HTTP://example.com"))
        assertTrue(isAcceptableHttpUrl("HTTPS://example.com"))
    }

    @Test
    fun testEmptyStringNotAcceptable() {
        assertFalse(isAcceptableUrl(""))
        assertFalse(isAcceptableHttpUrl(""))
    }

    @Test
    fun testSchemeOnlyNotAcceptable() {
        assertFalse(isAcceptableUrl("http://"))
        assertFalse(isAcceptableHttpUrl("http://"))
        assertFalse(isAcceptableHttpUrl("https://"))
    }

    @Test
    fun testInvalidSchemeNotAcceptable() {
        assertFalse(isAcceptableHttpUrl("ftp://example.com"))
    }

    @Test
    fun testNoSchemeNotAcceptable() {
        assertFalse(isAcceptableHttpUrl("example.com"))
    }

    @Test
    fun testVariableSchemeAcceptable() {
        assertTrue(isAcceptableHttpUrl("{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}://example.com"))
        assertTrue(isAcceptableHttpUrl("{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}example.com"))
        assertTrue(isAcceptableHttpUrl("http{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}://example.com"))

        assertTrue(isAcceptableHttpUrl("{{42}}://example.com"))
        assertTrue(isAcceptableHttpUrl("{{42}}example.com"))
        assertTrue(isAcceptableHttpUrl("http{{42}}://example.com"))
    }

    @Test
    fun testVariableOnlyUrlAcceptable() {
        assertTrue(isAcceptableHttpUrl("{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}"))

        assertTrue(isAcceptableHttpUrl("{{42}}"))
    }

    @Test
    fun testPartialVariableSchemeAcceptable() {
        assertTrue(isAcceptableHttpUrl("http{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}://example.com"))

        assertTrue(isAcceptableHttpUrl("http{{42}}://example.com"))
    }

    @Test
    fun testNoSchemeNotValid() {
        assertFalse(isValidUrl("example.com".toUri()))
    }

    @Test
    fun testNonHttpSchemeNotValidHttpUrl() {
        assertFalse(isValidHttpUrl("ftp://example.com".toUri()))
    }

    @Test
    fun testNonHttpSchemeValidUrl() {
        assertTrue(isValidUrl("ftp://example.com".toUri()))
    }

    @Test
    fun testEmptyUrlNotValid() {
        assertFalse(isValidHttpUrl("http://".toUri()))
        assertFalse(isValidHttpUrl("https://".toUri()))
        assertFalse(isValidHttpUrl("https:".toUri()))
        assertFalse(isValidHttpUrl("https".toUri()))
        assertFalse(isValidHttpUrl("https:/".toUri()))
        assertFalse(isValidHttpUrl("https:///".toUri()))
        assertFalse(isValidHttpUrl("https://:".toUri()))
    }

    @Test
    fun testHttpSchemeValid() {
        assertTrue(isValidHttpUrl("http://example.com".toUri()))
    }

    @Test
    fun testHttpSchemeValidCaseInsensitive() {
        assertTrue(isValidHttpUrl("HTTP://example.com".toUri()))
    }

    @Test
    fun testHttpsSchemeValid() {
        assertTrue(isValidHttpUrl("https://example.com".toUri()))
    }

    @Test
    fun testHttpsSchemeValidCaseInsensitive() {
        assertTrue(isValidHttpUrl("HTTPS://example.com".toUri()))
    }

    @Test
    fun testNotValidWithInvalidCharacters() {
        assertFalse(isValidHttpUrl("http://{{1234⁻5678}}".toUri()))
        assertFalse(isValidHttpUrl(Uri.parse("https://\"+document.domain+\"/")))
        assertFalse(isValidHttpUrl("http://a</".toUri()))
    }

    @Test
    fun testUrlWithWhitespacesIsValid() {
        assertTrue(isValidHttpUrl("http://example.com/?cmd=Hello World".toUri()))
    }
}
