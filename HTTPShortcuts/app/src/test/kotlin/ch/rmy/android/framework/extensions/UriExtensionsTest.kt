package ch.rmy.android.framework.extensions

import androidx.core.net.toUri
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UriExtensionsTest {

    @Test
    fun `http URL is considered a web URL`() {
        val url = "http://example.com".toUri()
        assertTrue(url.isWebUrl)
    }

    @Test
    fun `https URL is considered a web URL`() {
        val url = "https://example.com".toUri()
        assertTrue(url.isWebUrl)
    }

    @Test
    fun `uppercase http URL is considered a web URL`() {
        val url = "HTTP://example.com".toUri()
        assertTrue(url.isWebUrl)
    }

    @Test
    fun `uppercase https URL is considered a web URL`() {
        val url = "HTTPS://example.com".toUri()
        assertTrue(url.isWebUrl)
    }

    @Test
    fun `file URL is not considered a web URL`() {
        val url = "file://foobar".toUri()
        assertFalse(url.isWebUrl)
    }
}
