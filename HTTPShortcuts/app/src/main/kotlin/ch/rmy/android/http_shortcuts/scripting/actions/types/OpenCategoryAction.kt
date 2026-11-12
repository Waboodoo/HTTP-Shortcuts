package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import android.content.Intent
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenCategoryAction
@Inject
constructor(
    private val context: Context,
    private val activityProvider: ActivityProvider,
    private val categoryRepository: CategoryRepository,
) : Action<OpenCategoryAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            activityProvider.withActivity { activity ->
                val category = try {
                    categoryRepository.getCategoryByNameOrId(categoryNameOrId)
                } catch (_: NoSuchElementException) {
                    throw ActionException {
                        getString(R.string.error_category_not_found_for_open, categoryNameOrId)
                    }
                }
                if (category.hidden) {
                    throw ActionException {
                        getString(R.string.error_category_not_found_for_open, categoryNameOrId)
                    }
                }
                MainActivity.IntentBuilder(newTask = false)
                    .categoryId(category.id)
                    .build(context)
                    .apply {
                        removeFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    .startActivity(activity)
            }
        }
    }

    data class Params(
        val categoryNameOrId: String,
    )
}
