package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Intent
import androidx.core.net.toUri
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SendIntentActionTest {

    @Test
    fun `intent construction`() {
        val parameters = JSONObject(
            """{
            "action": "test",
            "category": "foo",
            "categories": ["foobar", "bla"],
            "dataUri": "http://test-uri",
            "dataType": "text/plain",
            "packageName": "com.package.test",
            "clearTask": true,
            "excludeFromRecents": true,
            "newTask": true,
            "noHistory": true,
            "extras": [
                {
                    "type": "string",
                    "name": "stringExtra",
                    "value": "my-string"
                },
                {
                    "type": "boolean",
                    "name": "booleanExtra",
                    "value": true
                },
                {
                    "type": "long",
                    "name": "longExtra",
                    "value": 1337
                },
                {
                    "type": "double",
                    "name": "doubleExtra",
                    "value": 13.37
                },
                {
                    "type": "float",
                    "name": "floatExtra",
                    "value": 13.37
                }
            ]
        }
            """.trimIndent()
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
