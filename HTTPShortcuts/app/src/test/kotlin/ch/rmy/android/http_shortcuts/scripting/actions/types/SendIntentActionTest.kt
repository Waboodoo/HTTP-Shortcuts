package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Intent
import androidx.core.net.toUri
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SendIntentActionTest {

    @Test
    fun `intent construction`() {
        val parameters = mapOf(
            "action" to "test",
            "category" to "foo",
            "categories" to listOf("foobar", "bla"),
            "dataUri" to "http://test-uri",
            "dataType" to "text/plain",
            "packageName" to "com.package.test",
            "clearTask" to true,
            "excludeFromRecents" to true,
            "newTask" to true,
            "noHistory" to true,
            "extras" to listOf(
                mapOf(
                    "type" to "string",
                    "name" to "stringExtra",
                    "value" to "my-string",
                ),
                mapOf(
                    "type" to "boolean",
                    "name" to "booleanExtra",
                    "value" to true,
                ),
                mapOf(
                    "type" to "long",
                    "name" to "longExtra",
                    "value" to 1337,
                ),
                mapOf(
                    "type" to "double",
                    "name" to "doubleExtra",
                    "value" to 13.37,
                ),
                mapOf(
                    "type" to "float",
                    "name" to "floatExtra",
                    "value" to 13.37,
                ),
            ),
        )
        val intent = SendIntentAction.constructIntent(parameters)

        assertEquals(
            "test",
            intent.action,
        )
        assertEquals(
            setOf("foo", "foobar", "bla"),
            intent.categories,
        )
        assertEquals(
            "http://test-uri".toUri(),
            intent.data,
        )
        assertEquals(
            "text/plain",
            intent.type,
        )
        assertEquals(
            "com.package.test",
            intent.`package`,
        )

        assertNotEquals(
            0,
            intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK,
        )
        assertNotEquals(
            0,
            intent.flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
        )
        assertNotEquals(
            0,
            intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK,
        )
        assertNotEquals(
            0,
            intent.flags and Intent.FLAG_ACTIVITY_NO_HISTORY,
        )

        assertEquals(
            "my-string",
            intent.extras!!.getString("stringExtra"),
        )
        assertEquals(
            true,
            intent.extras!!.getBoolean("booleanExtra"),
        )
        assertEquals(
            1337L,
            intent.extras!!.getLong("longExtra"),
        )
        assertEquals(
            13.37,
            intent.extras!!.getDouble("doubleExtra"),
        )
        assertEquals(
            13.37f,
            intent.extras!!.getFloat("floatExtra"),
        )
    }
}
