package com.tools.h46incon.lockpapercountdown.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.tools.h46incon.lockpapercountdown.R;
import com.tools.h46incon.lockpapercountdown.tools.SetWallPaper;
import com.tools.h46incon.lockpapercountdown.tools.UpdateWallPaperReceiver;


/**
 * Created by h46incon on 2014/8/25.
 */
public class SettingFragment extends PreferenceFragment{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_preference);
		initPreference();
	}

	private void initPreference()
	{
		// Set preference change listener
		SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(
				listener
		);

		// disable set lock screen paper option?
		if (SetWallPaper.couldSetLockPaper() == false) {
			// Disable
			CheckBoxPreference prefSetLockPaper =
					(CheckBoxPreference)findPreference(getString(R.string.pref_key_is_update_lockpaper));
			prefSetLockPaper.setSummary(getString(R.string.pref_s_couldnot_set_lockpaper_msg));
			prefSetLockPaper.setChecked(false);
			prefSetLockPaper.setEnabled(false);
		}
	}

	private SharedPreferences.OnSharedPreferenceChangeListener listener =
		new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
			{
				String serviceKey = getString(R.string.pref_key_service_enable);
				String desDateKey = getString(R.string.pref_key_destination_date);
				boolean is_service_running = sharedPreferences.getBoolean(serviceKey, false);

				if (key.compareTo(serviceKey) == 0) {
					if (is_service_running) {
						Log.i(TAG, "start service");
						UpdateWallPaperReceiver.startAutoUpdate();
					} else {
						Log.i(TAG, "stop service");
						UpdateWallPaperReceiver.stopAutoUpdate();
					}
				} else if (key.compareTo(desDateKey) == 0) {
					// need refresh wallpaper
					if (is_service_running) {
						SetWallPaper.updatePaper();
					}
				}

			}
		};


	private static final String TAG = "SettingFragment";

}
