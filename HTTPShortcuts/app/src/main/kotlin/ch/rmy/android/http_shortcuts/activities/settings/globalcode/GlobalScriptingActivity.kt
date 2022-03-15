package ch.rmy.android.http_shortcuts.activities.settings.globalcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.insertAroundCursor
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.editor.scripting.CodeSnippetPicker
import ch.rmy.android.http_shortcuts.databinding.ActivityGlobalScriptingBinding
import ch.rmy.android.http_shortcuts.icons.IconPicker
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutSpanManager
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class GlobalScriptingActivity : BaseActivity() {

    private val viewModel: GlobalScriptingViewModel by bindViewModel()

    private val variablePlaceholderProvider = VariablePlaceholderProvider()
    private val shortcutPlaceholderProvider = ShortcutPlaceholderProvider()
    private val iconPicker: IconPicker by lazy {
        IconPicker(this) { iconName ->
            codeSnippetPicker.insertChangeIconSnippet(
                viewModel.iconPickerShortcutPlaceholder ?: return@IconPicker,
                getCodeInsertion(),
                iconName,
            )
        }
    }
    private val codeSnippetPicker by lazy {
        CodeSnippetPicker(
            context,
            null,
            variablePlaceholderProvider,
            shortcutPlaceholderProvider,
        ) { shortcutPlaceholder ->
            viewModel.iconPickerShortcutPlaceholder = shortcutPlaceholder
            iconPicker.openIconSelectionDialog()
        }
    }
    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }
    private val shortcutPlaceholderColor by lazy {
        color(context, R.color.shortcut)
    }

    private lateinit var binding: ActivityGlobalScriptingBinding
    private var saveButton: MenuItem? = null

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityGlobalScriptingBinding.inflate(layoutInflater))
        binding.buttonAddCodeSnippet.setOnClickListener {
            codeSnippetPicker.showCodeSnippetPicker(getCodeInsertion(), includeResponseOptions = false)
        }
    }

    private fun getCodeInsertion(): (String, String) -> Unit =
        { before, after ->
            binding.inputCode.insertAroundCursor(before, after)
            Variables.applyVariableFormattingToJS(binding.inputCode.text!!, variablePlaceholderProvider, variablePlaceholderColor)
            ShortcutSpanManager.applyShortcutFormattingToJS(binding.inputCode.text!!, shortcutPlaceholderProvider, shortcutPlaceholderColor)
        }

    private fun initUserInputBindings() {
        bindTextChangeListener(binding.inputCode)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            viewState.shortcuts?.let(shortcutPlaceholderProvider::applyShortcuts)
            binding.inputCode.setTextSafely(processTextForView(viewState.globalCode))
            saveButton?.isVisible = viewState.saveButtonVisible
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    private fun processTextForView(input: String): CharSequence {
        val text = SpannableStringBuilder(input)
        Variables.applyVariableFormattingToJS(
            text,
            variablePlaceholderProvider,
            variablePlaceholderColor,
        )
        ShortcutSpanManager.applyShortcutFormattingToJS(
            text,
            shortcutPlaceholderProvider,
            shortcutPlaceholderColor,
        )
        return text
    }

    private fun bindTextChangeListener(textView: EditText) {
        textView.observeTextChanges()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.onGlobalCodeChanged(it.toString())
            }
            .attachTo(destroyer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.global_scripting_activity_menu, menu)
        saveButton = menu.findItem(R.id.action_save_changes)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume {
            viewModel.onHelpButtonClicked()
        }
        R.id.action_save_changes -> consume {
            viewModel.onSaveButtonClicked()
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            codeSnippetPicker.handleRequestResult(
                getCodeInsertion(),
                requestCode,
                data,
            )
        }
        iconPicker.handleResult(requestCode, resultCode, data)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    class IntentBuilder : BaseIntentBuilder(GlobalScriptingActivity::class.java)
}
