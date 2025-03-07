package ch.rmy.android.http_shortcuts.utils

import com.google.gson.JsonSyntaxException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import org.junit.jupiter.api.assertThrows

class GsonUtilTest {

    @Test
    fun `pretty print json`() {
        val result = GsonUtil.prettyPrintOrThrow("""{"test": 123}""")
        assertEquals(
            """
                {
                  "test": 123
                }
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `pretty print json with invalid syntax`() {
        assertThrows<JsonSyntaxException> {
            GsonUtil.prettyPrintOrThrow("""{"test": 123,}""")
        }
    }

    @Test
    fun `pretty print error message 1`() {
        try {
            GsonUtil.prettyPrintOrThrow(
                """{
                |"test": 123,
                |}
                """.trimMargin(),
            )
            fail()
        } catch (e: JsonSyntaxException) {
            assertEquals(
                "Expected name at line 3 column 2 path \$.test",
                GsonUtil.extractErrorMessage(e),
            )
        }
    }

    @Test
    fun `pretty print error message 2`() {
        try {
            GsonUtil.prettyPrintOrThrow(
                """"test": 123""".trimMargin(),
            )
            fail()
        } catch (e: JsonSyntaxException) {
            assertEquals(
                "Malformed JSON at line 1 column 8 path \$",
                GsonUtil.extractErrorMessage(e),
            )
        }
    }

    @Test
    fun `pretty print error message 3`() {
        try {
            GsonUtil.prettyPrintOrThrow(
                """{
                |"test": 123
                |
                """.trimMargin(),
            )
            fail()
        } catch (e: JsonSyntaxException) {
            assertEquals(
                "End of input at line 3 column 1 path \$.test",
                GsonUtil.extractErrorMessage(e),
            )
        }
    }
}
