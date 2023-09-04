package ch.rmy.android.http_shortcuts.activities.categories.editor

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryEditorActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        CategoryEditorScreen(
            categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID),
        )
    }

    object OpenCategoryEditor : BaseActivityResultContract<IntentBuilder, Boolean>(::IntentBuilder) {
        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            resultCode == Activity.RESULT_OK
    }

    class IntentBuilder : BaseIntentBuilder(CategoryEditorActivity::class) {
        fun categoryId(categoryId: CategoryId) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }
    }

    companion object {
        private const val EXTRA_CATEGORY_ID = "category_id"
    }
}
