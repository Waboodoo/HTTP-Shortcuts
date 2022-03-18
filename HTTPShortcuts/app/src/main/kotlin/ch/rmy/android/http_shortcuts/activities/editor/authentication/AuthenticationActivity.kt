package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.os.Bundle
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.databinding.ActivityAuthenticationBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils

class AuthenticationActivity : BaseActivity() {

    private val viewModel: AuthenticationViewModel by bindViewModel()
    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityAuthenticationBinding.inflate(layoutInflater))
        setTitle(R.string.section_authentication)
        binding.inputAuthenticationMethod.setItemsFromPairs(
            AUTHENTICATION_METHODS.map {
                it.first to getString(it.second)
            }
        )
    }

    private fun initUserInputBindings() {
        VariableViewUtils.bindVariableViews(binding.inputUsername, binding.variableButtonUsername, variablePlaceholderProvider)
            .attachTo(destroyer)
        VariableViewUtils.bindVariableViews(binding.inputPassword, binding.variableButtonPassword, variablePlaceholderProvider)
            .attachTo(destroyer)
        VariableViewUtils.bindVariableViews(binding.inputToken, binding.variableButtonToken, variablePlaceholderProvider)
            .attachTo(destroyer)

        binding.inputAuthenticationMethod
            .selectionChanges
            .subscribe(viewModel::onAuthenticationMethodChanged)
            .attachTo(destroyer)

        binding.inputUsername.observeTextChanges()
            .subscribe {
                viewModel.onUsernameChanged(binding.inputUsername.rawString)
            }
            .attachTo(destroyer)

        binding.inputPassword.observeTextChanges()
            .subscribe {
                viewModel.onPasswordChanged(binding.inputPassword.rawString)
            }
            .attachTo(destroyer)

        binding.inputToken.observeTextChanges()
            .subscribe {
                viewModel.onTokenChanged(binding.inputToken.rawString)
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            binding.containerUsername.visible = viewState.isUsernameAndPasswordVisible
            binding.containerPassword.visible = viewState.isUsernameAndPasswordVisible
            binding.containerToken.visible = viewState.isTokenVisible
            binding.inputAuthenticationMethod.selectedItem = viewState.authenticationMethod
            binding.inputUsername.rawString = viewState.username
            binding.inputPassword.rawString = viewState.password
            binding.inputToken.rawString = viewState.token
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(AuthenticationActivity::class.java)

    companion object {

        private val AUTHENTICATION_METHODS = listOf(
            ShortcutModel.AUTHENTICATION_NONE to R.string.authentication_none,
            ShortcutModel.AUTHENTICATION_BASIC to R.string.authentication_basic,
            ShortcutModel.AUTHENTICATION_DIGEST to R.string.authentication_digest,
            ShortcutModel.AUTHENTICATION_BEARER to R.string.authentication_bearer,
        )
    }
}
