package ch.rmy.android.http_shortcuts.activities.settings

import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ch.rmy.android.framework.extensions.showSnackbar
import ch.rmy.android.framework.utils.Destroyer
import ch.rmy.android.http_shortcuts.extensions.applyTheme

abstract class BaseSettingsFragment : PreferenceFragmentCompat() {

    protected val destroyer = Destroyer()

    protected fun initPreference(key: String, isVisible: Boolean = true, action: () -> Unit = {}): Preference =
        findPreference<Preference>(key)!!
            .apply {
                this.isVisible = isVisible
                applyTheme()
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    action()
                    true
                }
            }

    protected fun initListPreference(key: String, action: (newValue: Any) -> Unit = {}): ListPreference =
        findPreference<ListPreference>(key)!!
            .apply {
                applyTheme()
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    if (isAdded) {
                        updateSummary(newValue)
                        action(newValue)
                    }
                    true
                }
                updateSummary(null)
            }

    private fun ListPreference.updateSummary(value: Any?) {
        val index = findIndexOfValue((value ?: this.value) as String?)
            .takeUnless { it == -1 }
        summary = entries[index ?: 0]
    }

    protected fun showSnackbar(message: CharSequence) {
        activity?.showSnackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyer.destroy()
    }
}
