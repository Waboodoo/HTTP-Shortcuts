package ch.rmy.android.http_shortcuts.activities.settings.about

import android.os.Bundle
import androidx.preference.Preference
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.InstallUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.misc.AcknowledgmentActivity
import ch.rmy.android.http_shortcuts.activities.settings.BaseSettingsFragment
import ch.rmy.android.http_shortcuts.activities.settings.ContactActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.VersionUtil
import javax.inject.Inject

class AboutActivity : BaseActivity() {

    val viewModel: AboutViewModel by bindViewModel()

    @Inject
    lateinit var versionUtil: VersionUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        getApplicationComponent().inject(this)
    }

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews(savedState == null)
        initViewModelBindings()
    }

    private fun initViews(firstInit: Boolean) {
        setContentView(R.layout.activity_about)
        setTitle(R.string.title_about)
        if (firstInit) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_view, AboutFragment())
                .commit()
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    class AboutFragment : BaseSettingsFragment() {

        private val viewModel: AboutViewModel
            get() = (activity as AboutActivity).viewModel

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.about, rootKey)

            initPreference("changelog") {
                viewModel.onChangeLogButtonClicked()
            }
                .summary = getString(R.string.settings_changelog_summary, versionName)

            initPreference("mail") {
                contactDeveloper()
            }

            initPreference("privacy_policy") {
                viewModel.onPrivacyPolicyButtonClicked()
            }

            initPreference("documentation") {
                viewModel.onDocumentationButtonClicked()
            }

            if (InstallUtil.isAppInstalledFromPlayStore(requireContext())) {
                findPreference<Preference>("f_droid")!!.isVisible = false
            } else {
                initPreference("f_droid") {
                    openURL(ExternalURLs.F_DROID)
                }
            }

            initPreference("play_store") {
                openURL(ExternalURLs.PLAY_STORE)
            }

            initPreference("github") {
                openURL(ExternalURLs.GITHUB)
            }

            initPreference("donate") {
                openURL(ExternalURLs.DONATION_PAGE)
            }

            initPreference("translate") {
                openURL(ExternalURLs.TRANSLATION)
            }

            initPreference("acknowledgments") {
                showAcknowledgments()
            }
        }

        private val versionName: String
            get() = (activity as AboutActivity).versionUtil.getVersionName()

        private fun contactDeveloper() {
            ContactActivity.IntentBuilder()
                .startActivity(this)
        }

        private fun showAcknowledgments() {
            AcknowledgmentActivity.IntentBuilder()
                .startActivity(this)
        }
    }

    class IntentBuilder : BaseIntentBuilder(AboutActivity::class)
}
