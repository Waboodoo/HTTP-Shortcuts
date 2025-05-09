package ch.rmy.android.http_shortcuts.plugin

import android.os.Bundle
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.extensions.getRequestHeadersForShortcut
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcut
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfo
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PluginEditActivity : BaseActivity(), TaskerPluginConfig<Input> {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var requestHeaderRepository: RequestHeaderRepository

    @Inject
    lateinit var requestParameterRepository: RequestParameterRepository

    @Inject
    lateinit var globalVariableRepository: GlobalVariableRepository

    private val selectShortcut = registerForActivityResult(MainActivity.SelectShortcut) { result ->
        if (result != null) {
            input = Input(
                shortcutId = result.shortcutId,
                shortcutName = result.shortcutName,
            )
            lifecycleScope.launch {
                val shortcut = shortcutRepository.getShortcutById(result.shortcutId)
                val variableKeysOrIds = VariableResolver.findResolvableVariableIdentifiersIncludingScripting(
                    shortcut = shortcut,
                    headers = requestHeaderRepository.getRequestHeadersForShortcut(shortcut),
                    parameters = requestParameterRepository.getRequestParametersForShortcut(shortcut),
                )
                variableKeys = variableKeysOrIds.mapNotNull { variableKeyOrId ->
                    variableKeyOrId.variableKey
                        ?: try {
                            globalVariableRepository.getVariableByKeyOrId(variableKeyOrId).key
                        } catch (_: NoSuchElementException) {
                            null
                        }
                }
                TriggerShortcutActionHelper(this@PluginEditActivity)
                    .finishForTasker()
            }
        }
    }

    private var input: Input? = null
    private var variableKeys: List<VariableKey> = emptyList()

    override fun onCreated(savedState: Bundle?) {
        selectShortcut.launch()
    }

    override val inputForTasker: TaskerInput<Input>
        get() = TaskerInput(
            input ?: Input(
                shortcutId = "???",
                shortcutName = "???",
            ),
            getTaskerInputInfos(),
        )

    private fun getTaskerInputInfos() =
        TaskerInputInfos().apply {
            variableKeys
                .forEach { variableKey ->
                    add(
                        TaskerInputInfo(
                            key = variableKey,
                            label = variableKey,
                            description = null,
                            ignoreInStringBlurb = false,
                            value = "%$variableKey",
                        ),
                    )
                }
        }

    override fun assignFromInput(input: TaskerInput<Input>) {
        this.input = input.regular
    }
}
