package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.addArguments
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.mapFor
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseFragment
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.databinding.FragmentListBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.ExportUI
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager

class ShortcutListFragment : BaseFragment<FragmentListBinding>() {

    val categoryId by lazy {
        args.getString(ARG_CATEGORY_ID) ?: ""
    }

    val layoutType: CategoryLayoutType by lazy {
        CategoryLayoutType.parse(args.getString(ARG_CATEGORY_LAYOUT_TYPE))
    }

    val selectionMode by lazy {
        args.getSerializable(ARG_SELECTION_MODE) as SelectionMode
    }

    private var isDraggingEnabled = false

    private var previousBackground: CategoryBackgroundType? = null

    private val exportUI by lazy {
        destroyer.own(ExportUI(requireActivity()))
    }

    private val viewModel: ShortcutListViewModel by bindViewModel { "$categoryId-$layoutType-$selectionMode" }

    private lateinit var adapter: BaseShortcutAdapter

    private val wallpaper: Drawable? by lazy {
        try {
            WallpaperManager.getInstance(context).drawable
        } catch (e: SecurityException) {
            null
        }
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentListBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(ShortcutListViewModel.InitData(categoryId, selectionMode))
    }

    override fun setupViews() {
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        adapter = when (layoutType) {
            CategoryLayoutType.LINEAR_LIST -> ShortcutListAdapter()
            CategoryLayoutType.GRID -> ShortcutGridAdapter()
        }

        binding.shortcutList.layoutManager = when (layoutType) {
            CategoryLayoutType.GRID -> GridLayoutManager(requireContext(), R.dimen.grid_layout_shortcut_width)
            CategoryLayoutType.LINEAR_LIST -> LinearLayoutManager(context)
        }
        binding.shortcutList.adapter = adapter
        binding.shortcutList.setHasFixedSize(true)
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        adapter.userEvents.observe(this) { event ->
            when (event) {
                is BaseShortcutAdapter.UserEvent.ShortcutClicked -> viewModel.onShortcutClicked(event.id)
                is BaseShortcutAdapter.UserEvent.ShortcutLongClicked -> viewModel.onShortcutLongClicked(event.id)
            }
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            allowHorizontalDragging = layoutType == CategoryLayoutType.GRID,
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? BaseShortcutAdapter.BaseShortcutViewHolder)?.shortcutId },
        )
        dragOrderingHelper.attachTo(binding.shortcutList)
        dragOrderingHelper.movementSource
            .subscribe { (shortcutId1, shortcutId2) ->
                viewModel.onShortcutMoved(shortcutId1, shortcutId2)
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.shortcuts
            adapter.isLongClickingEnabled = viewState.isLongClickingEnabled
            binding.shortcutList.alpha = if (viewState.isInMovingMode) 0.7f else 1f
            (binding.shortcutList.layoutManager as? GridLayoutManager)?.setEmpty(viewState.isEmptyStateVisible)
            isDraggingEnabled = viewState.isDraggingEnabled
            updateBackground(viewState.background)
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    private fun updateBackground(background: CategoryBackgroundType) {
        if (background == previousBackground) {
            return
        }
        previousBackground = background
        binding.background.apply {
            when (background) {
                CategoryBackgroundType.WHITE -> {
                    setImageDrawable(null)
                    setBackgroundColor(color(requireContext(), R.color.activity_background))
                }
                CategoryBackgroundType.BLACK -> {
                    setImageDrawable(null)
                    setBackgroundColor(color(requireContext(), R.color.activity_background_dark))
                }
                CategoryBackgroundType.WALLPAPER -> {
                    wallpaper
                        ?.also {
                            setImageDrawable(it)
                        }
                        ?: run {
                            setImageDrawable(null)
                            setBackgroundColor(color(requireContext(), R.color.activity_background))
                        }
                }
            }
        }
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ShortcutListEvent.ShowContextMenu -> showContextMenu(
                event.shortcutId,
                event.title,
                event.isPending,
                event.isMovable,
            )
            is ShortcutListEvent.ShowMoveToCategoryDialog -> showMoveToCategoryDialog(event.shortcutId, event.categoryOptions)
            is ShortcutListEvent.ShowFileExportDialog -> showFileExportDialog(event.shortcutId, event.format, event.variableIds)
            is ShortcutListEvent.StartExport -> startExport(event.shortcutId, event.uri, event.format, event.variableIds)
            else -> super.handleEvent(event)
        }
    }

    private fun showContextMenu(shortcutId: String, title: String, isPending: Boolean, isMovable: Boolean) {
        DialogBuilder(requireContext())
            .title(title)
            .item(R.string.action_place) {
                viewModel.onPlaceOnHomeScreenOptionSelected(shortcutId)
            }
            .item(R.string.action_run) {
                viewModel.onExecuteOptionSelected(shortcutId)
            }
            .mapIf(isPending) {
                item(R.string.action_cancel_pending) {
                    viewModel.onCancelPendingExecutionOptionSelected(shortcutId)
                }
            }
            .separator()
            .item(R.string.action_edit) {
                viewModel.onEditOptionSelected(shortcutId)
            }
            .mapIf(isMovable) {
                item(R.string.action_move) {
                    viewModel.onMoveOptionSelected(shortcutId)
                }
            }
            .item(R.string.action_duplicate) {
                viewModel.onDuplicateOptionSelected(shortcutId)
            }
            .item(R.string.action_delete) {
                viewModel.onDeleteOptionSelected(shortcutId)
            }
            .separator()
            .item(R.string.action_shortcut_information) {
                viewModel.onShowInfoOptionSelected(shortcutId)
            }
            .item(R.string.action_export) {
                viewModel.onExportOptionSelected(shortcutId)
            }
            .showIfPossible()
    }

    private fun showMoveToCategoryDialog(shortcutId: String, categoryOptions: List<ShortcutListEvent.ShowMoveToCategoryDialog.CategoryOption>) {
        DialogBuilder(requireContext())
            .title(R.string.title_move_to_category)
            .mapFor(categoryOptions) { categoryOption ->
                item(name = categoryOption.name) {
                    viewModel.onMoveTargetCategorySelected(shortcutId, categoryOption.categoryId)
                }
            }
            .showIfPossible()
    }

    private fun showFileExportDialog(shortcutId: String, format: ExportFormat, variableIds: Collection<String>) {
        exportUI.showExportOptions(format, shortcutId, variableIds) { intent ->
            viewModel.onFileExportStarted(shortcutId)
            try {
                intent.startActivity(this, REQUEST_EXPORT)
            } catch (e: ActivityNotFoundException) {
                context?.showToast(R.string.error_not_supported)
            }
        }
    }

    private fun startExport(shortcutId: String, uri: Uri, format: ExportFormat, variableIds: Collection<String>) {
        exportUI.startExport(uri, format = format, shortcutId = shortcutId, variableIds = variableIds)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_EDIT_SHORTCUT -> {
                viewModel.onShortcutEdited()
            }
            REQUEST_EXPORT -> {
                if (resultCode == RESULT_OK) {
                    viewModel.onExportDestinationSelected(intent?.data ?: return)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPaused()
    }

    override fun onBackPressed() =
        viewModel.onBackPressed()

    companion object {

        fun create(categoryId: String, layoutType: CategoryLayoutType, selectionMode: SelectionMode): ShortcutListFragment =
            ShortcutListFragment().addArguments {
                putString(ARG_CATEGORY_ID, categoryId)
                putString(ARG_CATEGORY_LAYOUT_TYPE, layoutType.toString())
                putSerializable(ARG_SELECTION_MODE, selectionMode)
            }

        const val REQUEST_EDIT_SHORTCUT = 2
        private const val REQUEST_EXPORT = 3

        private const val ARG_CATEGORY_ID = "categoryId"
        private const val ARG_CATEGORY_LAYOUT_TYPE = "categoryLayoutType"
        private const val ARG_SELECTION_MODE = "selectionMode"
    }
}
