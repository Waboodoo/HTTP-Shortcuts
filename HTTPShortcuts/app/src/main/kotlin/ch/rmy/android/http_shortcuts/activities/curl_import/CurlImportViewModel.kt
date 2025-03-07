package ch.rmy.android.http_shortcuts.activities.curl_import

import android.app.Application
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.curlcommand.CommandParser
import ch.rmy.curlcommand.CurlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class CurlImportViewModel
@Inject
constructor(
    application: Application,
) : BaseViewModel<Unit, CurlImportViewState>(application) {

    private var detectUnsupportedOptionsJob: Job? = null

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    override suspend fun initialize(data: Unit) = CurlImportViewState()

    fun onInputTextChanged(inputText: String) = runAction {
        _inputText.value = inputText
        detectUnsupportedOptionsJob?.cancel()
        detectUnsupportedOptionsJob = async(Dispatchers.Default) {
            delay(200.milliseconds)
            val command = CommandParser.parseCommand("$inputText$$")
            val unsupportedOptions = command
                .filter { option ->
                    !option.endsWith("$$") && option != "-" && option != "--" && option.startsWith("-")
                }
                .filter { option ->
                    !CurlParser.isSupportedOption(option)
                }
            updateViewState {
                copy(unsupportedOptions = unsupportedOptions)
            }
        }
        updateViewState {
            copy(submitButtonEnabled = inputText.isNotEmpty())
        }
    }

    fun onSubmitButtonClicked() = runAction {
        val command = inputText.value.takeUnlessEmpty()
            ?.let(CurlParser::parse)
            ?: skipAction()

        closeScreen(result = command)
    }
}
