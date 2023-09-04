package ch.rmy.android.http_shortcuts.activities.curl_import

import android.app.Application
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.curlcommand.CurlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CurlImportViewModel
@Inject
constructor(
    application: Application,
) : BaseViewModel<Unit, CurlImportViewState>(application) {

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    override suspend fun initialize(data: Unit) = CurlImportViewState()

    fun onInputTextChanged(inputText: String) = runAction {
        _inputText.value = inputText
        updateViewState {
            copy(submitButtonEnabled = inputText.isNotEmpty())
        }
    }

    fun onSubmitButtonClicked() = runAction {
        val command = inputText.value.takeUnlessEmpty()
            ?.let(CurlParser::parse)
            ?: skipAction()

        finishWithOkResult(CurlImportActivity.ImportFromCurl.createResult(command))
    }
}
