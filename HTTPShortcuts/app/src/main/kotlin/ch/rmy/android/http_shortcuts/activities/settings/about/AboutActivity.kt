package ch.rmy.android.http_shortcuts.activities.settings.about

import android.os.Bundle
import androidx.preference.Preference
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.InstallUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.misc.AcknowledgmentActivity
import ch.rmy.android.http_shortcuts.activities.settings.BaseSettingsFragment
import ch.rmy.android.http_shortcuts.activities.settings.ContactActivity
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.VersionUtil

class AboutActivity : BaseActivity() {

    val viewModel: AboutViewModel by bindViewModel()

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initViewModelBindings()
    }

    private fun initViews() {
        setContentView(R.layout.activity_about)
        setTitle(R.string.title_about)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_view, AboutFragment())
            .commit()
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
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
                openURL(ExternalURLs.PRIVACY_POLICY)
            }

            initPreference("documentation") {
                openURL(ExternalURLs.DOCUMENTATION_PAGE)
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

            initPreference("translate") {
                openURL(ExternalURLs.TRANSLATION)
            }

            initPreference("acknowledgments") {
                showAcknowledgments()
            }
        }

        private val versionName: String
            get() = VersionUtil.getVersionName(requireContext())

        private fun contactDeveloper() {
            ContactActivity.IntentBuilder()
                .startActivity(this)
        }

        private fun showAcknowledgments() {
            AcknowledgmentActivity.IntentBuilder()
                .startActivity(this)
        }
    }

    class IntentBuilder : BaseIntentBuilder(AboutActivity::class.java)
}
