package ch.rmy.android.http_shortcuts.activities.categories

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityCategoriesBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.utils.PermissionManager

class CategoriesActivity : BaseActivity() {

    private val viewModel: CategoriesViewModel by bindViewModel()

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var adapter: CategoryAdapter
    private var isDraggingEnabled = false

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityCategoriesBinding.inflate(layoutInflater))
        setTitle(R.string.title_categories)

        adapter = CategoryAdapter()

        val manager = LinearLayoutManager(context)
        binding.categoryList.layoutManager = manager
        binding.categoryList.setHasFixedSize(true)
        binding.categoryList.adapter = adapter

        binding.buttonCreateCategory.applyTheme(themeHelper)
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        adapter.userEvents.observe(this) { event ->
            when (event) {
                is CategoryAdapter.UserEvent.CategoryClicked -> {
                    viewModel.onCategoryClicked(event.id)
                }
            }
        }

        binding.buttonCreateCategory.setOnClickListener {
            viewModel.onCreateCategoryButtonClicked()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? CategoryAdapter.CategoryViewHolder)?.categoryId },
        )
        dragOrderingHelper.attachTo(binding.categoryList)
        dragOrderingHelper.movementSource.observe(this) { (categoryId1, categoryId2) ->
            viewModel.onCategoryMoved(categoryId1, categoryId2)
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.categories
            isDraggingEnabled = viewState.isDraggingEnabled
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is CategoriesEvent.RequestFilePermissionsIfNeeded -> {
                PermissionManager.requestFileStoragePermissionIfNeeded(this)
            }
            else -> super.handleEvent(event)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.categories_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { viewModel.onHelpButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    object OpenCategories : BaseActivityResultContract<IntentBuilder, Boolean>(::IntentBuilder) {
        private const val EXTRA_CATEGORIES_CHANGED = "categories_changed"

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            intent?.getBooleanExtra(EXTRA_CATEGORIES_CHANGED, false) ?: false

        fun createResult(categoriesChanged: Boolean) =
            createIntent {
                putExtra(EXTRA_CATEGORIES_CHANGED, categoriesChanged)
            }
    }

    class IntentBuilder : BaseIntentBuilder(CategoriesActivity::class.java)
}
