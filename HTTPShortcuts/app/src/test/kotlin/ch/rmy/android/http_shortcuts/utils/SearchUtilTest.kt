package ch.rmy.android.http_shortcuts.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class SearchUtilTest {

    @Test
    fun `normalize keywords`() {
        val input = "Show a Selection (Multiple-Choice)"
        val result = SearchUtil.normalizeToKeywords(input)
        assertThat(result, equalTo(setOf("show", "selection", "multiple", "choice")))
    }

    @Test
    fun `normalize keywords with numbers`() {
        val input = "md5 sha512 BASE64"
        val result = SearchUtil.normalizeToKeywords(input)
        assertThat(result, equalTo(setOf("md5", "sha512", "base64")))
    }

    @Test
    fun `normalize keywords with no min length`() {
        val input = "I want all of the words in a sentence"
        val result = SearchUtil.normalizeToKeywords(input, minLength = 1)
        assertThat(result, equalTo(setOf("i", "want", "all", "of", "the", "words", "in", "a", "sentence")))
    }
}
