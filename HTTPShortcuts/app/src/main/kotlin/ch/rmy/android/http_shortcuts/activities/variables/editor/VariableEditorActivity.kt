package ch.rmy.android.http_shortcuts.activities.variables.editor

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.doOnCheckedChanged
import ch.rmy.android.framework.extensions.doOnTextChanged
import ch.rmy.android.framework.extensions.focus
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.color.ColorTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant.ConstantTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.date.DateTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.select.SelectTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.slider.SliderTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.text.TextTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.time.TimeTypeFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.toggle.ToggleTypeFragment
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.databinding.ActivityVariableEditorBinding
import kotlinx.coroutines.launch

class VariableEditorActivity : BaseActivity() {

    private val variableId: VariableId? by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra(EXTRA_VARIABLE_ID)
    }
    private val variableType: VariableType by lazy(LazyThreadSafetyMode.NONE) {
        VariableType.parse(intent.getStringExtra(EXTRA_VARIABLE_TYPE))
    }

    private lateinit var defaultColor: ColorStateList

    private val viewModel: VariableEditorViewModel by bindViewModel()

    private lateinit var binding: ActivityVariableEditorBinding

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(
            VariableEditorViewModel.InitData(
                variableId = variableId,
                variableType = variableType,
            ),
        )
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityVariableEditorBinding.inflate(layoutInflater))
        binding.mainView.isVisible = false
        title = ""

        defaultColor = binding.inputVariableKey.textColors

        binding.inputShareSupport.setItemsFromPairs(
            SHARE_SUPPORT_OPTIONS.map {
                it.first.name to getString(it.second)
            }
        )

        initVariableTypeFragment()
    }

    private fun initVariableTypeFragment() {
        val tag = "variable_edit_fragment_${variableType.type}"
        val fragment = supportFragmentManager.findFragmentByTag(tag) as? BaseFragment<*>
            ?: createEditorFragment()
            ?: return

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.variable_type_fragment_container, fragment, tag)
            .commitAllowingStateLoss()
    }

    private fun createEditorFragment(): BaseFragment<*>? =
        when (variableType) {
            VariableType.CONSTANT -> ConstantTypeFragment.create(variableId)
            VariableType.TEXT -> TextTypeFragment()
            VariableType.NUMBER -> TextTypeFragment()
            VariableType.PASSWORD -> TextTypeFragment()
            VariableType.SELECT -> SelectTypeFragment()
            VariableType.TOGGLE -> ToggleTypeFragment()
            VariableType.COLOR -> ColorTypeFragment()
            VariableType.DATE -> DateTypeFragment()
            VariableType.TIME -> TimeTypeFragment()
            VariableType.SLIDER -> SliderTypeFragment()
            VariableType.CLIPBOARD -> null
            VariableType.UUID -> null
        }

    private fun initUserInputBindings() {
        binding.inputVariableKey.doOnTextChanged { text ->
            viewModel.onVariableKeyChanged(text.toString())
        }
        binding.inputVariableTitle.doOnTextChanged { text ->
            viewModel.onVariableTitleChanged(text.toString())
        }
        binding.inputVariableMessage.doOnTextChanged { text ->
            viewModel.onVariableMessageChanged(text.toString())
        }

        binding.inputUrlEncode.doOnCheckedChanged(viewModel::onUrlEncodeChanged)
        binding.inputJsonEncode.doOnCheckedChanged(viewModel::onJsonEncodeChanged)
        binding.inputAllowShare.doOnCheckedChanged(viewModel::onAllowShareChanged)

        lifecycleScope.launch {
            binding.inputShareSupport.selectionChanges.collect { selectedOption ->
                viewModel.onShareSupportChanged(VariableEditorViewState.ShareSupport.valueOf(selectedOption))
            }
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.loadingIndicator.isVisible = false
            binding.mainView.isVisible = true
            setTitle(viewState.title)
            setSubtitle(viewState.subtitle)
            binding.dialogTitleContainer.isVisible = viewState.dialogTitleVisible
            binding.dialogMessageContainer.isVisible = viewState.dialogMessageVisible
            binding.inputVariableKey.error = viewState.variableKeyInputError?.localize(context)
            binding.inputVariableKey.setTextSafely(viewState.variableKey)
            binding.inputVariableTitle.setTextSafely(viewState.variableTitle)
            binding.inputVariableMessage.setTextSafely(viewState.variableMessage)
            if (viewState.variableKeyErrorHighlighting) {
                binding.inputVariableKey.setTextColor(Color.RED)
            } else {
                binding.inputVariableKey.setTextColor(defaultColor)
            }
            binding.inputUrlEncode.isChecked = viewState.urlEncodeChecked
            binding.inputJsonEncode.isChecked = viewState.jsonEncodeChecked
            binding.inputAllowShare.isChecked = viewState.allowShareChecked
            if (viewState.shareSupportVisible) {
                binding.inputShareSupport.selectedItem = viewState.shareSupport.name
            }
            binding.inputShareSupport.isVisible = viewState.shareSupportVisible
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is VariableEditorEvent.FocusVariableKeyInput -> binding.inputVariableKey.focus()
            else -> super.handleEvent(event)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.variable_editor_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_save_variable -> consume { viewModel.onSaveButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder(type: VariableType) : BaseIntentBuilder(VariableEditorActivity::class) {

        init {
            intent.putExtra(EXTRA_VARIABLE_TYPE, type.type)
        }

        fun variableId(variableId: VariableId) = also {
            intent.putExtra(EXTRA_VARIABLE_ID, variableId)
        }
    }

    companion object {

        private const val EXTRA_VARIABLE_ID = "ch.rmy.android.http_shortcuts.activities.variables.editor.VariableEditorActivity.variable_id"
        private const val EXTRA_VARIABLE_TYPE = "ch.rmy.android.http_shortcuts.activities.variables.editor.VariableEditorActivity.variable_type"

        private val SHARE_SUPPORT_OPTIONS = listOf(
            VariableEditorViewState.ShareSupport.TEXT to R.string.label_share_support_option_text,
            VariableEditorViewState.ShareSupport.TITLE to R.string.label_share_support_option_title,
            VariableEditorViewState.ShareSupport.TITLE_AND_TEXT to R.string.label_share_support_option_title_and_text,
        )
    }
}
