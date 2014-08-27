package com.tools.h46incon.lockpapercountdown.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.tools.h46incon.lockpapercountdown.R;
import com.tools.h46incon.lockpapercountdown.tools.SetWallPaper;
import com.tools.h46incon.lockpapercountdown.tools.UpdateWallPaperReceiver;
import com.tools.h46incon.lockpapercountdown.util.MyApplication;


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
			prefSetLockPaper.setSummary("本系统不支持设置锁屏壁纸");
			prefSetLockPaper.setChecked(false);
			prefSetLockPaper.setEnabled(false);
		}
	}

	private SharedPreferences.OnSharedPreferenceChangeListener listener =
		new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
			{
				String serviceKey =
						MyApplication.getContext().getString(R.string.pref_key_service_enable);
				if (serviceKey.compareTo(key) != 0) {
					return;
				}

				boolean is_service_running = sharedPreferences.getBoolean(key, false);
				if (is_service_running) {
					Log.i(TAG, "start service");
					UpdateWallPaperReceiver.startAutoUpdate();
				} else {
					Log.i(TAG, "stop service");
					UpdateWallPaperReceiver.stopAutoUpdate();
				}
			}
		};


	private static final String TAG = "SettingFragment";
}
