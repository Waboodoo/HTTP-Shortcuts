package ch.rmy.android.http_shortcuts.activities.curl_import

import android.app.Application
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.curlcommand.CurlParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurlImportViewModel(application: Application) : BaseViewModel<Unit, CurlImportViewState>(application) {

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    override fun initViewState() = CurlImportViewState()

    fun onInputTextChanged(inputText: String) {
        _inputText.value = inputText
        updateViewState {
            copy(submitButtonEnabled = inputText.isNotEmpty())
        }
    }

    fun onSubmitButtonClicked() {
        val command = inputText.value.takeUnlessEmpty()
            ?.let(CurlParser::parse)
            ?: return

        finishWithOkResult(CurlImportActivity.ImportFromCurl.createResult(command))
    }
}
