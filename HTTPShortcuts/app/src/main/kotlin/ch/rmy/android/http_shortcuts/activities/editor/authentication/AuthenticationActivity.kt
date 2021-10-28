package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityAuthenticationBinding
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class AuthenticationActivity : BaseActivity() {

    private val viewModel: AuthenticationViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityAuthenticationBinding.inflate(layoutInflater))
        setTitle(R.string.section_authentication)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        binding.inputAuthenticationMethod.setItemsFromPairs(AUTHENTICATION_METHODS.map {
            it.first to getString(it.second)
        })
        VariableViewUtils.bindVariableViews(binding.inputUsername, binding.variableButtonUsername, variablePlaceholderProvider)
            .attachTo(destroyer)
        VariableViewUtils.bindVariableViews(binding.inputPassword, binding.variableButtonPassword, variablePlaceholderProvider)
            .attachTo(destroyer)
        VariableViewUtils.bindVariableViews(binding.inputToken, binding.variableButtonToken, variablePlaceholderProvider)
            .attachTo(destroyer)
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this) {
            updateShortcutViews()
        }
        variablesData.observe(this) {
            updateShortcutViews()
        }

        binding.inputAuthenticationMethod.selectionChanges
            .concatMapCompletable { method -> viewModel.setAuthenticationMethod(method) }
            .subscribe()
            .attachTo(destroyer)
        bindTextChangeListener(binding.inputUsername) { shortcutData.value?.username }
        bindTextChangeListener(binding.inputPassword) { shortcutData.value?.password }
        bindTextChangeListener(binding.inputToken) { shortcutData.value?.authToken }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it.toString() != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setCredentials(binding.inputUsername.rawString, binding.inputPassword.rawString, binding.inputToken.rawString)

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        binding.inputAuthenticationMethod.selectedItem = shortcut.authentication ?: Shortcut.AUTHENTICATION_NONE
        when {
            shortcut.usesBasicAuthentication() || shortcut.usesDigestAuthentication() -> {
                binding.containerUsername.visible = true
                binding.containerPassword.visible = true
                binding.containerToken.visible = false
                binding.inputUsername.rawString = shortcut.username
                binding.inputPassword.rawString = shortcut.password
            }
            shortcut.usesBearerAuthentication() -> {
                binding.containerUsername.visible = false
                binding.containerPassword.visible = false
                binding.containerToken.visible = true
                binding.inputToken.rawString = shortcut.authToken
            }
            else -> {
                binding.containerUsername.visible = false
                binding.containerPassword.visible = false
                binding.containerToken.visible = false
            }
        }
    }

    override fun onBackPressed() {
        updateViewModelFromViews()
            .subscribe {
                finish()
            }
            .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, AuthenticationActivity::class.java)

    companion object {

        private val AUTHENTICATION_METHODS = listOf(
            Shortcut.AUTHENTICATION_NONE to R.string.authentication_none,
            Shortcut.AUTHENTICATION_BASIC to R.string.authentication_basic,
            Shortcut.AUTHENTICATION_DIGEST to R.string.authentication_digest,
            Shortcut.AUTHENTICATION_BEARER to R.string.authentication_bearer,
        )

    }

}