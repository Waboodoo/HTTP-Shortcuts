package ch.rmy.android.framework.extensions

import androidx.core.net.toUri
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UriExtensionsTest {

    @Test
    fun `http URL is considered a web URL`() {
        val url = "http://example.com".toUri()
        assertThat(url.isWebUrl, equalTo(true))
    }

    @Test
    fun `https URL is considered a web URL`() {
        val url = "https://example.com".toUri()
        assertThat(url.isWebUrl, equalTo(true))
    }

    @Test
    fun `uppercase http URL is considered a web URL`() {
        val url = "HTTP://example.com".toUri()
        assertThat(url.isWebUrl, equalTo(true))
    }

    @Test
    fun `uppercase https URL is considered a web URL`() {
        val url = "HTTPS://example.com".toUri()
        assertThat(url.isWebUrl, equalTo(true))
    }

    @Test
    fun `file URL is not considered a web URL`() {
        val url = "file://foobar".toUri()
        assertThat(url.isWebUrl, equalTo(false))
    }
}
