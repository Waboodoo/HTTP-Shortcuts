package ch.rmy.android.http_shortcuts.activities.main

import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.addArguments
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.whileLifecycleActive
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.databinding.FragmentListBinding
import ch.rmy.android.http_shortcuts.import_export.OpenFilePickerForExportContract
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager

class ShortcutListFragment : BaseFragment<FragmentListBinding>() {

    private val openShortcutEditor = registerForActivityResult(ShortcutEditorActivity.OpenShortcutEditor) { shortcutId ->
        if (shortcutId != null) {
            viewModel.onShortcutEdited()
        }
    }

    private val openFilePickerForExport = registerForActivityResult(OpenFilePickerForExportContract) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForExport)
    }

    val categoryId: CategoryId by lazy(LazyThreadSafetyMode.NONE) {
        args.getString(ARG_CATEGORY_ID) ?: ""
    }

    val layoutType: CategoryLayoutType by lazy(LazyThreadSafetyMode.NONE) {
        CategoryLayoutType.parse(args.getString(ARG_CATEGORY_LAYOUT_TYPE))
    }

    val selectionMode by lazy(LazyThreadSafetyMode.NONE) {
        args.getSerializable(ARG_SELECTION_MODE) as SelectionMode
    }

    private var isDraggingEnabled = false

    private var previousBackground: CategoryBackgroundType? = null

    private val viewModel: ShortcutListViewModel by bindViewModel { "$categoryId-$layoutType-$selectionMode" }

    private lateinit var adapter: BaseShortcutAdapter

    private val wallpaper: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
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
        previousBackground = null
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        adapter = when (layoutType) {
            CategoryLayoutType.LINEAR_LIST -> ShortcutListAdapter()
            CategoryLayoutType.DENSE_GRID,
            CategoryLayoutType.MEDIUM_GRID,
            CategoryLayoutType.WIDE_GRID,
            -> ShortcutGridAdapter()
        }

        binding.shortcutList.layoutManager = when (layoutType) {
            CategoryLayoutType.LINEAR_LIST -> LinearLayoutManager(context)
            CategoryLayoutType.DENSE_GRID -> GridLayoutManager(requireContext(), R.dimen.grid_layout_shortcut_width_dense)
            CategoryLayoutType.MEDIUM_GRID -> GridLayoutManager(requireContext(), R.dimen.grid_layout_shortcut_width_medium)
            CategoryLayoutType.WIDE_GRID -> GridLayoutManager(requireContext(), R.dimen.grid_layout_shortcut_width_wide)
        }
        binding.shortcutList.adapter = adapter
        binding.shortcutList.setHasFixedSize(true)
    }

    private fun initUserInputBindings() {
        initDragOrdering()
        whileLifecycleActive {
            adapter.userEvents.collect { event ->
                when (event) {
                    is BaseShortcutAdapter.UserEvent.ShortcutClicked -> viewModel.onShortcutClicked(event.id)
                    is BaseShortcutAdapter.UserEvent.ShortcutLongClicked -> viewModel.onShortcutLongClicked(event.id)
                }
            }
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            allowHorizontalDragging = layoutType.supportsHorizontalDragging,
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? BaseShortcutAdapter.BaseShortcutViewHolder)?.shortcutId },
        )
        dragOrderingHelper.attachTo(binding.shortcutList)
        whileLifecycleActive {
            dragOrderingHelper.movementSource.collect { (shortcutId1, shortcutId2) ->
                viewModel.onShortcutMoved(shortcutId1, shortcutId2)
            }
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            adapter.items = viewState.shortcuts
            adapter.isLongClickingEnabled = viewState.isLongClickingEnabled
            binding.shortcutList.alpha = if (viewState.isInMovingMode) 0.7f else 1f
            (binding.shortcutList.layoutManager as? GridLayoutManager)?.setEmpty(viewState.isEmptyStateVisible)
            isDraggingEnabled = viewState.isDraggingEnabled
            updateBackground(viewState.background)
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    private fun updateBackground(background: CategoryBackgroundType) {
        if (background == previousBackground) {
            return
        }
        previousBackground = background
        binding.background.apply {
            when (background) {
                is CategoryBackgroundType.Default -> {
                    setImageDrawable(null)
                    setBackgroundColor(color(requireContext(), R.color.activity_background))
                }
                is CategoryBackgroundType.Color -> {
                    setImageDrawable(null)
                    setBackgroundColor(background.color)
                }
                is CategoryBackgroundType.Wallpaper -> {
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
            is ShortcutListEvent.OpenShortcutEditor -> openShortcutEditor.launch {
                shortcutId(event.shortcutId)
                    .categoryId(event.categoryId)
            }
            is ShortcutListEvent.OpenFilePickerForExport -> openFilePickerForExport.launch(
                OpenFilePickerForExportContract.Params(
                    format = event.exportFormat,
                    single = true,
                )
            )
            else -> super.handleEvent(event)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPaused()
    }

    override fun onBackPressed() =
        viewModel.onBackPressed()

    companion object {

        fun create(categoryId: CategoryId, layoutType: CategoryLayoutType, selectionMode: SelectionMode): ShortcutListFragment =
            ShortcutListFragment().addArguments {
                putString(ARG_CATEGORY_ID, categoryId)
                putString(ARG_CATEGORY_LAYOUT_TYPE, layoutType.toString())
                putSerializable(ARG_SELECTION_MODE, selectionMode)
            }

        private const val ARG_CATEGORY_ID = "categoryId"
        private const val ARG_CATEGORY_LAYOUT_TYPE = "categoryLayoutType"
        private const val ARG_SELECTION_MODE = "selectionMode"
    }
}
