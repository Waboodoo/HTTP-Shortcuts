package ch.rmy.android.http_shortcuts.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class MqttUtilTest {

    @Test
    fun `parse body into messages`() {
        val target = "###MESSAGE->my-topic\nSome päyload\n###MESSAGE->my-other-topic\nNewline\nCharacters\n"

        val actual = MqttUtil.getMessagesFromBody(target)
        assertEquals(
            listOf(
                MqttUtil.Message("my-topic", "Some päyload".toByteArray()),
                MqttUtil.Message("my-other-topic", "Newline\nCharacters\n".toByteArray()),
            ),
            actual,
        )
    }

    @Test
    fun `count messages`() {
        val target = "###MESSAGE->my-topic\nSome päyload\n###MESSAGE->my-other-topic\nNewline\nCharacters\n"

        val actual = MqttUtil.countMessagesInBody(target)
        assertEquals(2, actual)
    }

    @Test
    fun `encode messages into body`() {
        val target = listOf(
            MqttUtil.Message("my-topic", "Some päyload".toByteArray()),
            MqttUtil.Message("my-other-topic", "Newline\nCharacters\n".toByteArray()),
        )
        val expected = "###MESSAGE->my-topic\nSome päyload\n###MESSAGE->my-other-topic\nNewline\nCharacters\n"
        val actual = MqttUtil.getBodyFromMessages(target)
        assertEquals(expected, actual)
    }

    @Test
    fun `encode and decode yields same result`() {
        val target = listOf(
            MqttUtil.Message("my-topic", "Some päyload".toByteArray()),
            MqttUtil.Message("my-other-topic", "Newline\nCharacters\n".toByteArray()),
        )
        val actual = MqttUtil.getMessagesFromBody(MqttUtil.getBodyFromMessages(target))
        assertEquals(target, actual)
    }
}
