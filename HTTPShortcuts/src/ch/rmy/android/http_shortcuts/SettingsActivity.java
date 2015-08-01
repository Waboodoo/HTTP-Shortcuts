package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SettingsFragment settingsFragment = new SettingsFragment();
		getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
	}

	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);

			final ListPreference clickBehaviorPreference = (ListPreference) findPreference("click_behavior");
			clickBehaviorPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					clickBehaviorPreference.setSummary(clickBehaviorPreference.getEntries()[clickBehaviorPreference.findIndexOfValue((String) newValue)]);
					return true;
				}

			});
			clickBehaviorPreference.setSummary(clickBehaviorPreference.getEntries()[clickBehaviorPreference.findIndexOfValue(clickBehaviorPreference.getValue())]);

		}

	}

}
