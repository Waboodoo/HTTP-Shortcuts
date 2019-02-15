package ch.rmy.android.http_shortcuts.utils

import android.net.Uri
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isValidUrl
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ValidationTest {

    @Test
    fun testValidUrlAcceptable() {
        assertThat(isAcceptableUrl("http://example.com"), equalTo(true))
        assertThat(isAcceptableUrl("https://example.com"), equalTo(true))
        assertThat(isAcceptableUrl("HTTP://example.com"), equalTo(true))
        assertThat(isAcceptableUrl("HTTPS://example.com"), equalTo(true))
    }

    @Test
    fun testEmptyStringNotAcceptable() {
        assertThat(isAcceptableUrl(""), equalTo(false))
    }

    @Test
    fun testSchemeOnlyNotAcceptable() {
        assertThat(isAcceptableUrl("http://"), equalTo(false))
        assertThat(isAcceptableUrl("https://"), equalTo(false))
    }

    @Test
    fun testInvalidSchemeNotAcceptable() {
        assertThat(isAcceptableUrl("ftp://example.com"), equalTo(false))
    }

    @Test
    fun testNoSchemeNotAcceptable() {
        assertThat(isAcceptableUrl("example.com"), equalTo(false))
    }

    @Test
    fun testVariableSchemeAcceptable() {
        assertThat(isAcceptableUrl("{{scheme}}://example.com"), equalTo(true))
        assertThat(isAcceptableUrl("{{scheme2}}example.com"), equalTo(true))
        assertThat(isAcceptableUrl("http{{scheme3}}://example.com"), equalTo(true))
    }

    @Test
    fun testPartialVariableSchemeAcceptable() {
        assertThat(isAcceptableUrl("http{{useHttps}}://example.com"), equalTo(true))
    }

    @Test
    fun testNoSchemeNotValid() {
        assertThat(isValidUrl(Uri.parse("example.com")), equalTo(false))
    }

    @Test
    fun testNonHttpSchemeNotValid() {
        assertThat(isValidUrl(Uri.parse("ftp://example.com")), equalTo(false))
    }

    @Test
    fun testHttpSchemeValid() {
        assertThat(isValidUrl(Uri.parse("http://example.com")), equalTo(true))
    }

    @Test
    fun testHttpSchemeValidCaseInsensitive() {
        assertThat(isValidUrl(Uri.parse("HTTP://example.com")), equalTo(true))
    }

    @Test
    fun testHttpsSchemeValid() {
        assertThat(isValidUrl(Uri.parse("https://example.com")), equalTo(true))
    }

    @Test
    fun testHttpsSchemeValidCaseInsensitive() {
        assertThat(isValidUrl(Uri.parse("HTTPS://example.com")), equalTo(true))
    }

}