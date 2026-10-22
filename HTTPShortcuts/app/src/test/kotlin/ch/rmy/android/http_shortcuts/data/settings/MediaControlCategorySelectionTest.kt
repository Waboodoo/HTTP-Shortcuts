package ch.rmy.android.http_shortcuts.data.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class MediaControlCategorySelectionTest {

    @Test
    fun `missing or invalid value defaults to all categories`() {
        assertEquals(MediaControlCategorySelection(), MediaControlCategorySelection.parse(null))
        assertEquals(MediaControlCategorySelection(), MediaControlCategorySelection.parse("invalid\nfirst"))
    }

    @Test
    fun `selection survives serialization`() {
        val selection = MediaControlCategorySelection(
            includeAllCategories = false,
            selectedCategoryIds = setOf("second", "first\nwith separator"),
        )

        assertEquals(selection, MediaControlCategorySelection.parse(selection.serialize()))
    }

    @Test
    fun `manual selection is retained while all categories are included`() {
        val selection = MediaControlCategorySelection(
            includeAllCategories = true,
            selectedCategoryIds = setOf("first"),
        )

        assertEquals(selection, MediaControlCategorySelection.parse(selection.serialize()))
    }

    @Test
    fun `disabled and empty category IDs in stored data are ignored`() {
        assertEquals(
            MediaControlCategorySelection(
                includeAllCategories = false,
                selectedCategoryIds = setOf("selected"),
            ),
            MediaControlCategorySelection.parse(
                """{"include_all":false,"selected_category_ids":{"selected":true,"disabled":false,"":true}}""",
            ),
        )
    }
}
