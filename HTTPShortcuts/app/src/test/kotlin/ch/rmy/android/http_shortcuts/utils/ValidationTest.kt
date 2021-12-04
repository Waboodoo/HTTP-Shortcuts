package ch.rmy.android.http_shortcuts.utils

import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableHttpUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isValidHttpUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isValidUrl
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ValidationTest {

    @Test
    fun testValidUrlAcceptable() {
        assertThat(isAcceptableHttpUrl("http://example.com"), equalTo(true))
        assertThat(isAcceptableHttpUrl("https://example.com"), equalTo(true))
        assertThat(isAcceptableHttpUrl("HTTP://example.com"), equalTo(true))
        assertThat(isAcceptableHttpUrl("HTTPS://example.com"), equalTo(true))
    }

    @Test
    fun testEmptyStringNotAcceptable() {
        assertThat(isAcceptableUrl(""), equalTo(false))
        assertThat(isAcceptableHttpUrl(""), equalTo(false))
    }

    @Test
    fun testSchemeOnlyNotAcceptable() {
        assertThat(isAcceptableUrl("http://"), equalTo(false))
        assertThat(isAcceptableHttpUrl("http://"), equalTo(false))
        assertThat(isAcceptableHttpUrl("https://"), equalTo(false))
    }

    @Test
    fun testInvalidSchemeNotAcceptable() {
        assertThat(isAcceptableHttpUrl("ftp://example.com"), equalTo(false))
    }

    @Test
    fun testNoSchemeNotAcceptable() {
        assertThat(isAcceptableHttpUrl("example.com"), equalTo(false))
    }

    @Test
    fun testVariableSchemeAcceptable() {
        assertThat(isAcceptableHttpUrl("{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}://example.com"), equalTo(true))
        assertThat(isAcceptableHttpUrl("{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}example.com"), equalTo(true))
        assertThat(isAcceptableHttpUrl("http{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}://example.com"), equalTo(true))

        assertThat(isAcceptableHttpUrl("{{42}}://example.com"), equalTo(true))
        assertThat(isAcceptableHttpUrl("{{42}}example.com"), equalTo(true))
        assertThat(isAcceptableHttpUrl("http{{42}}://example.com"), equalTo(true))
    }

    @Test
    fun testVariableOnlyUrlAcceptable() {
        assertThat(isAcceptableHttpUrl("{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}"), equalTo(true))

        assertThat(isAcceptableHttpUrl("{{42}}"), equalTo(true))
    }

    @Test
    fun testPartialVariableSchemeAcceptable() {
        assertThat(isAcceptableHttpUrl("http{{12a21268-84a3-4e79-b7cd-51b87fc49eb7}}://example.com"), equalTo(true))

        assertThat(isAcceptableHttpUrl("http{{42}}://example.com"), equalTo(true))
    }

    @Test
    fun testNoSchemeNotValid() {
        assertThat(isValidUrl("example.com".toUri()), equalTo(false))
    }

    @Test
    fun testNonHttpSchemeNotValidHttpUrl() {
        assertThat(isValidHttpUrl("ftp://example.com".toUri()), equalTo(false))
    }

    @Test
    fun testNonHttpSchemeValidUrl() {
        assertThat(isValidUrl("ftp://example.com".toUri()), equalTo(true))
    }

    @Test
    fun testEmptyUrlNotValid() {
        assertThat(isValidHttpUrl("http://".toUri()), equalTo(false))
        assertThat(isValidHttpUrl("https://".toUri()), equalTo(false))
        assertThat(isValidHttpUrl("https:".toUri()), equalTo(false))
        assertThat(isValidHttpUrl("https".toUri()), equalTo(false))
        assertThat(isValidHttpUrl("https:/".toUri()), equalTo(false))
        assertThat(isValidHttpUrl("https:///".toUri()), equalTo(false))
        assertThat(isValidHttpUrl("https://:".toUri()), equalTo(false))
    }

    @Test
    fun testHttpSchemeValid() {
        assertThat(isValidHttpUrl("http://example.com".toUri()), equalTo(true))
    }

    @Test
    fun testHttpSchemeValidCaseInsensitive() {
        assertThat(isValidHttpUrl("HTTP://example.com".toUri()), equalTo(true))
    }

    @Test
    fun testHttpsSchemeValid() {
        assertThat(isValidHttpUrl("https://example.com".toUri()), equalTo(true))
    }

    @Test
    fun testHttpsSchemeValidCaseInsensitive() {
        assertThat(isValidHttpUrl("HTTPS://example.com".toUri()), equalTo(true))
    }

    @Test
    fun testNotValidWithInvalidCharacters() {
        assertThat(isValidHttpUrl("http://{{1234⁻5678}}".toUri()), equalTo(false))
        assertThat(isValidHttpUrl(Uri.parse("https://\"+document.domain+\"/")), equalTo(false))
        assertThat(isValidHttpUrl("http://a</".toUri()), equalTo(false))
    }

    @Test
    fun testUrlWithWhitespacesIsValid() {
        assertThat(isValidHttpUrl("http://example.com/?cmd=Hello World".toUri()), equalTo(true))
    }
}
