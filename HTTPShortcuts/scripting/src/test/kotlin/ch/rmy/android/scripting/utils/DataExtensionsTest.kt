package ch.rmy.android.scripting.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class DataExtensionsTest {

    @Test
    fun `cycles are removed`() {
        val list = mutableListOf<Any?>()
        val map = mutableMapOf<String, Any?>()
        map.put("A", list)
        map.put("B", "B")
        map.put("C", listOf(3, 4))
        map.put("D", listOf(list, map, "X"))
        list.add("Foo")
        list.add(listOf(1, 2))
        list.add(map)
        list.add(list)

        val result = list.withoutCycles()
        assertEquals(
            listOf(
                "Foo",
                listOf(1, 2),
                mapOf(
                    "A" to null,
                    "B" to "B",
                    "C" to listOf(3, 4),
                    "D" to listOf(null, null, "X"),
                ),
                null,
            ),
            result,
        )
    }
}
