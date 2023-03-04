package ch.rmy.android.http_shortcuts.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchUtilTest {

    @Test
    fun `normalize keywords`() {
        val input = "Show a Selection (Multiple-Choice)"
        val result = SearchUtil.normalizeToKeywords(input)
        assertEquals(
            setOf("show", "selection", "multiple", "choice"),
            result,
        )
    }

    @Test
    fun `normalize keywords with numbers`() {
        val input = "md5 sha512 BASE64"
        val result = SearchUtil.normalizeToKeywords(input)
        assertEquals(
            setOf("md5", "sha512", "base64"),
            result,
        )
    }

    @Test
    fun `normalize keywords with no min length`() {
        val input = "I want all of the words in a sentence"
        val result = SearchUtil.normalizeToKeywords(input, minLength = 1)
        assertEquals(
            setOf("i", "want", "all", "of", "the", "words", "in", "a", "sentence"),
            result,
        )
    }
}
