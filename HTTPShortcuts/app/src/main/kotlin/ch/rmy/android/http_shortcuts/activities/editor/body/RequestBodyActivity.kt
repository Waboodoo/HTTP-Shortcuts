package ch.rmy.android.http_shortcuts.activities.editor.body

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.databinding.ActivityRequestBodyBinding
import ch.rmy.android.http_shortcuts.dialogs.KeyValueDialog
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils

class RequestBodyActivity : BaseActivity() {

    private val viewModel: RequestBodyViewModel by bindViewModel()
    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityRequestBodyBinding
    private lateinit var adapter: ParameterAdapter

    private var isDraggingEnabled = false

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityRequestBodyBinding.inflate(layoutInflater))
        setTitle(R.string.section_request_body)

        binding.inputRequestBodyType.setItemsFromPairs(
            REQUEST_BODY_TYPES.map {
                it.first.type to getString(it.second)
            }
        )

        adapter = ParameterAdapter(variablePlaceholderProvider)

        val manager = LinearLayoutManager(context)
        binding.parameterList.layoutManager = manager
        binding.parameterList.setHasFixedSize(true)
        binding.parameterList.adapter = adapter

        initDragOrdering()

        binding.buttonAddParameter.applyTheme(themeHelper)
        binding.buttonAddParameter.setOnClickListener {
            viewModel.onAddParameterButtonClicked()
        }

        VariableViewUtils.bindVariableViews(binding.inputBodyContent, binding.variableButtonBodyContent, variablePlaceholderProvider)

        binding.inputContentType.setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, CONTENT_TYPE_SUGGESTIONS))
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        adapter.userEvents
            .subscribe { event ->
                when (event) {
                    is ParameterAdapter.UserEvent.ParameterClicked -> viewModel.onParameterClicked(event.id)
                }
            }
            .attachTo(destroyer)

        binding.inputRequestBodyType.selectionChanges
            .subscribe { requestBodyType ->
                viewModel.onRequestBodyTypeChanged(RequestBodyType.parse(requestBodyType))
            }
            .attachTo(destroyer)

        binding.inputContentType
            .observeTextChanges()
            .subscribe { newContentType ->
                viewModel.onContentTypeChanged(newContentType.toString())
            }
            .attachTo(destroyer)

        binding.inputBodyContent
            .observeTextChanges()
            .subscribe {
                viewModel.onBodyContentChanged(binding.inputBodyContent.rawString)
            }
            .attachTo(destroyer)

        binding.buttonAddParameter.setOnClickListener {
            viewModel.onAddParameterButtonClicked()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? ParameterAdapter.ParameterViewHolder)?.parameterId },
        )
        dragOrderingHelper.attachTo(binding.parameterList)
        dragOrderingHelper.movementSource
            .subscribe { (parameterId1, parameterId2) ->
                viewModel.onParameterMoved(parameterId1, parameterId2)
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            adapter.items = viewState.parameters
            isDraggingEnabled = viewState.isDraggingEnabled
            binding.inputRequestBodyType.selectedItem = viewState.requestBodyType.type
            binding.inputContentType.setTextSafely(viewState.contentType)
            binding.inputBodyContent.rawString = viewState.bodyContent
            binding.parameterList.visible = viewState.parameterListVisible
            binding.buttonAddParameter.visible = viewState.addParameterButtonVisible
            binding.containerInputContentType.visible = viewState.contentTypeVisible
            binding.containerInputBodyContent.visible = viewState.bodyContentVisible
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is RequestBodyEvent.ShowAddParameterForStringDialog -> {
                showAddDialogForStringParameter()
            }
            is RequestBodyEvent.ShowAddParameterForFileDialog -> {
                showAddDialogForFileParameter(event.multiple)
            }
            is RequestBodyEvent.ShowEditParameterForStringDialog -> {
                showEditDialogForStringParameter(event.parameterId, event.key, event.value)
            }
            is RequestBodyEvent.ShowEditParameterForFileDialog -> {
                showEditDialogForFileParameter(event.parameterId, event.key, event.showFileNameOption, event.fileName)
            }
            else -> super.handleEvent(event)
        }
    }

    private fun showAddDialogForStringParameter() {
        KeyValueDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_post_param_add),
            keyLabel = getString(R.string.label_post_param_key),
            valueLabel = getString(R.string.label_post_param_value),
            isMultiLine = true,
        )
            .show(context)
            .subscribe { event ->
                when (event) {
                    is KeyValueDialog.Event.DataChangedEvent -> viewModel.onAddStringParameterDialogConfirmed(event.data.first, event.data.second)
                    is KeyValueDialog.Event.DataRemovedEvent -> Unit
                }
            }
            .attachTo(destroyer)
    }

    private fun showAddDialogForFileParameter(multiple: Boolean) {
        FileParameterDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(if (multiple) R.string.title_post_param_add_files else R.string.title_post_param_add_file),
            showFileNameOption = !multiple,
        )
            .show(context)
            .subscribe { event ->
                when (event) {
                    is FileParameterDialog.Event.DataChangedEvent -> viewModel.onAddFileParameterDialogConfirmed(
                        event.keyName,
                        event.fileName,
                        multiple,
                    )
                    is FileParameterDialog.Event.DataRemovedEvent -> Unit
                }
            }
            .attachTo(destroyer)
    }

    private fun showEditDialogForStringParameter(parameterId: String, key: String, value: String) {
        KeyValueDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_post_param_edit),
            keyLabel = getString(R.string.label_post_param_key),
            valueLabel = getString(R.string.label_post_param_value),
            data = key to value,
            isMultiLine = true,
        )
            .show(context)
            .subscribe { event ->
                when (event) {
                    is KeyValueDialog.Event.DataChangedEvent -> {
                        viewModel.onEditParameterDialogConfirmed(parameterId, event.data.first, value = event.data.second)
                    }
                    is KeyValueDialog.Event.DataRemovedEvent -> {
                        viewModel.onRemoveParameterButtonClicked(parameterId)
                    }
                }
            }
            .attachTo(destroyer)
    }

    private fun showEditDialogForFileParameter(
        parameterId: String,
        key: String,
        showFileNameOption: Boolean,
        fileName: String,
    ) {
        FileParameterDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_post_param_edit_file),
            showRemoveOption = true,
            showFileNameOption = showFileNameOption,
            keyName = key,
            fileName = fileName,
        )
            .show(context)
            .subscribe { event ->
                when (event) {
                    is FileParameterDialog.Event.DataChangedEvent -> {
                        viewModel.onEditParameterDialogConfirmed(parameterId, event.keyName, fileName = event.fileName)
                    }
                    is FileParameterDialog.Event.DataRemovedEvent -> {
                        viewModel.onRemoveParameterButtonClicked(parameterId)
                    }
                }
            }
            .attachTo(destroyer)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(RequestBodyActivity::class.java)

    companion object {

        private val REQUEST_BODY_TYPES = listOf(
            RequestBodyType.CUSTOM_TEXT to R.string.request_body_option_custom_text,
            RequestBodyType.FORM_DATA to R.string.request_body_option_form_data,
            RequestBodyType.X_WWW_FORM_URLENCODE to R.string.request_body_option_x_www_form_urlencoded,
            RequestBodyType.FILE to R.string.request_body_option_file,
        )

        private val CONTENT_TYPE_SUGGESTIONS = listOf(
            "application/javascript",
            "application/json",
            "application/octet-stream",
            "application/xml",
            "text/css",
            "text/csv",
            "text/plain",
            "text/html",
            "text/xml",
        )
    }
}
