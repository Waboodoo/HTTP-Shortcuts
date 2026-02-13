package ch.rmy.android.framework.extensions

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionExtensionsTest {
    @Test
    fun `is sub sequence`() {
        assertTrue(listOf(1, 2, 3, 4).isSubSequenceOf(listOf(1, 2, 3, 4)))
        assertTrue(listOf(1, 2, 4).isSubSequenceOf(listOf(1, 2, 3, 4, 5)))
        assertTrue(listOf(2, 4, 6).isSubSequenceOf(listOf(1, 2, 3, 4, 5, 6, 7)))
        assertTrue(emptyList<Int>().isSubSequenceOf(listOf(1, 2, 3)))
    }

    @Test
    fun `is not sub sequence`() {
        assertFalse(listOf(1, 2, 3, 4).isSubSequenceOf(listOf(1, 2, 3)))
        assertFalse(listOf(1, 3, 2, 4).isSubSequenceOf(listOf(1, 2, 3, 4, 5)))
        assertFalse(listOf(3).isSubSequenceOf(listOf(1, 2, 4)))
    }
}
