package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Intent
import androidx.core.net.toUri
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SendIntentActionTest {

    @Test
    fun testIntentConstruction() {
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

        assertThat(intent.action, equalTo("test"))
        assertThat(intent.categories, equalTo(setOf("foo", "foobar", "bla")))
        assertThat(intent.data, equalTo("http://test-uri".toUri()))
        assertThat(intent.type, equalTo("text/plain"))
        assertThat(intent.`package`, equalTo("com.package.test"))

        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK, not(equalTo(0)))
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS, not(equalTo(0)))
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK, not(equalTo(0)))
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NO_HISTORY, not(equalTo(0)))

        assertThat(intent.extras!!.getString("stringExtra"), equalTo("my-string"))
        assertThat(intent.extras!!.getBoolean("booleanExtra"), equalTo(true))
        assertThat(intent.extras!!.getLong("longExtra"), equalTo(1337L))
        assertThat(intent.extras!!.getDouble("doubleExtra"), equalTo(13.37))
        assertThat(intent.extras!!.getFloat("floatExtra"), equalTo(13.37f))
    }
}
