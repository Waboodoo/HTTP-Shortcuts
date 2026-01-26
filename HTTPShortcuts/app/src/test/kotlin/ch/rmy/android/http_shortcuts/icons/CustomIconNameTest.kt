package ch.rmy.android.http_shortcuts.icons

import android.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CustomIconNameTest {
    @Test
    fun `parse plain custom icon name`() {
        val customIconName = CustomIconName("custom-icon_123456.png")
        assertFalse(customIconName.isCircular)
        assertFalse(customIconName.hasTransparency)
        assertNull(customIconName.singleColor)
    }

    @Test
    fun `parse custom icon name with circle`() {
        val customIconName = CustomIconName("custom-icon_123456_circle.png")
        assertTrue(customIconName.isCircular)
        assertFalse(customIconName.hasTransparency)
        assertNull(customIconName.singleColor)
    }

    @Test
    fun `parse custom icon name with transparency`() {
        val customIconName = CustomIconName("custom-icon_123456_tr.png")
        assertFalse(customIconName.isCircular)
        assertTrue(customIconName.hasTransparency)
        assertNull(customIconName.singleColor)
    }

    @Test
    fun `parse custom icon name with single color`() {
        val customIconName = CustomIconName("custom-icon_12345678_tr-ff0000.png")
        assertFalse(customIconName.isCircular)
        assertTrue(customIconName.hasTransparency)
        assertEquals(Color.RED, customIconName.singleColor)
    }
}
