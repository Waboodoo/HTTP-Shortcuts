package ch.rmy.android.http_shortcuts.activities.editor.body

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.doOnTextChanged
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.extensions.whileLifecycleActive
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.databinding.ActivityRequestBodyBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

class RequestBodyActivity : BaseActivity() {

    @Inject
    lateinit var adapter: ParameterAdapter

    private val viewModel: RequestBodyViewModel by bindViewModel()

    private lateinit var binding: ActivityRequestBodyBinding

    private var isDraggingEnabled = false

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

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

        val manager = LinearLayoutManager(context)
        binding.parameterList.layoutManager = manager
        binding.parameterList.setHasFixedSize(true)
        binding.parameterList.adapter = adapter

        initDragOrdering()

        binding.buttonAddParameter.applyTheme(themeHelper)
        binding.buttonAddParameter.setOnClickListener {
            viewModel.onAddParameterButtonClicked()
        }

        binding.variableButtonBodyContent.setOnClickListener {
            viewModel.onBodyContentVariableButtonClicked()
        }

        binding.inputContentType.setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, CONTENT_TYPE_SUGGESTIONS))
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        whileLifecycleActive {
            adapter.userEvents.collect { event ->
                when (event) {
                    is ParameterAdapter.UserEvent.ParameterClicked -> viewModel.onParameterClicked(event.id)
                }
            }
        }

        lifecycleScope.launch {
            binding.inputRequestBodyType.selectionChanges.collect { requestBodyType ->
                viewModel.onRequestBodyTypeChanged(RequestBodyType.parse(requestBodyType))
            }
        }

        binding.inputContentType.doOnTextChanged { newContentType ->
            viewModel.onContentTypeChanged(newContentType.toString())
        }

        binding.inputBodyContent.doOnTextChanged {
            viewModel.onBodyContentChanged(binding.inputBodyContent.rawString)
        }

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
        whileLifecycleActive {
            dragOrderingHelper.movementSource.collect { (parameterId1, parameterId2) ->
                viewModel.onParameterMoved(parameterId1, parameterId2)
            }
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            adapter.items = viewState.parameters
            isDraggingEnabled = viewState.isDraggingEnabled
            binding.inputRequestBodyType.selectedItem = viewState.requestBodyType.type
            binding.inputContentType.setTextSafely(viewState.contentType)
            binding.inputBodyContent.rawString = viewState.bodyContent
            binding.parameterList.isVisible = viewState.parameterListVisible
            binding.buttonAddParameter.isVisible = viewState.addParameterButtonVisible
            binding.containerInputContentType.isVisible = viewState.contentTypeVisible
            binding.containerInputBodyContent.isVisible = viewState.bodyContentVisible
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is RequestBodyEvent.InsertVariablePlaceholder -> binding.inputBodyContent.insertVariablePlaceholder(event.variablePlaceholder)
            else -> super.handleEvent(event)
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(RequestBodyActivity::class)

    companion object {

        private val REQUEST_BODY_TYPES = listOf(
            RequestBodyType.CUSTOM_TEXT to R.string.request_body_option_custom_text,
            RequestBodyType.FORM_DATA to R.string.request_body_option_form_data,
            RequestBodyType.X_WWW_FORM_URLENCODE to R.string.request_body_option_x_www_form_urlencoded,
            RequestBodyType.FILE to R.string.request_body_option_file,
            RequestBodyType.IMAGE to R.string.request_body_option_image,
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
