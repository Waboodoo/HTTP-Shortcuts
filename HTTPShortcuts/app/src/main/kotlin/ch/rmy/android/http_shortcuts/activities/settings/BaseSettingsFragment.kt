package ch.rmy.android.http_shortcuts.activities.settings

import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.utils.Destroyer

abstract class BaseSettingsFragment : PreferenceFragmentCompat() {

    protected val destroyer = Destroyer()

    protected fun initPreference(key: String, action: () -> Unit = {}): Preference {
        val preference = findPreference<Preference>(key)!!
        preference.applyTheme()
        preference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            action()
            true
        }
        return preference
    }

    protected fun initListPreference(key: String, action: (newValue: Any) -> Unit = {}): ListPreference {
        val preference = findPreference<ListPreference>(key)!!
        preference.applyTheme()
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            if (isAdded) {
                updateSummary(preference, newValue)
                action(newValue)
            }
            true
        }
        updateSummary(preference, null)
        return preference
    }

    private fun updateSummary(preference: ListPreference, value: Any?) {
        val index = preference.findIndexOfValue((value ?: preference.value) as String?)
            .takeUnless { it == -1 }
        preference.summary = preference.entries[index ?: 0]
    }

    protected fun showSnackbar(message: CharSequence) {
        activity?.showSnackbar(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyer.destroy()
    }
}
