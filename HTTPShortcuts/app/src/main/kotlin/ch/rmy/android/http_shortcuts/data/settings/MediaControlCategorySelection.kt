package ch.rmy.android.http_shortcuts.data.settings

import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import org.json.JSONException
import org.json.JSONObject

data class MediaControlCategorySelection(
    val includeAllCategories: Boolean = true,
    val selectedCategoryIds: Set<CategoryId> = emptySet(),
) {
    fun serialize(): String = JSONObject()
        .put(KEY_INCLUDE_ALL, includeAllCategories)
        .put(
            KEY_SELECTED_CATEGORY_IDS,
            JSONObject().apply {
                selectedCategoryIds.sorted().forEach { categoryId ->
                    put(categoryId, true)
                }
            },
        )
        .toString()

    companion object {
        private const val KEY_INCLUDE_ALL = "include_all"
        private const val KEY_SELECTED_CATEGORY_IDS = "selected_category_ids"

        fun parse(value: String?): MediaControlCategorySelection {
            if (value == null) {
                return MediaControlCategorySelection()
            }
            return try {
                val jsonObject = JSONObject(value)
                val categoryIds = jsonObject.optJSONObject(KEY_SELECTED_CATEGORY_IDS)
                MediaControlCategorySelection(
                    includeAllCategories = jsonObject.optBoolean(KEY_INCLUDE_ALL, true),
                    selectedCategoryIds = categoryIds
                        ?.keys()
                        ?.asSequence()
                        ?.filter { categoryId ->
                            categoryId.isNotEmpty() && categoryIds.optBoolean(categoryId)
                        }
                        ?.toSet()
                        .orEmpty(),
                )
            } catch (_: JSONException) {
                MediaControlCategorySelection()
            }
        }
    }
}
