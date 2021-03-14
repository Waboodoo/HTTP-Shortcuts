package ch.rmy.android.http_shortcuts.activities.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.misc.AcknowledgmentActivity
import ch.rmy.android.http_shortcuts.dialogs.ChangeLogDialog
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.ExternalURLs


class AboutActivity : BaseActivity() {

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setTitle(R.string.title_about)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_view, AboutFragment())
            .commit()
    }

    class AboutFragment : BaseSettingsFragment() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.about, rootKey)

            initPreference("changelog") {
                ChangeLogDialog(requireContext(), false)
                    .show()
                    .subscribe({}, {
                        showSnackbar(R.string.error_generic, long = true)
                    })
                    .attachTo(destroyer)
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
            get() = try {
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
            } catch (e: NameNotFoundException) {
                "???"
            }

        private fun contactDeveloper() {
            ContactActivity.IntentBuilder(requireContext())
                .startActivity(this)
        }

        private fun showAcknowledgments() {
            AcknowledgmentActivity.IntentBuilder(requireContext())
                .startActivity(this)
        }

        private fun openURL(url: String) {
            try {
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .startActivity(this)
            } catch (e: ActivityNotFoundException) {
                showSnackbar(R.string.error_not_supported)
            }
        }

    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, AboutActivity::class.java)

}
