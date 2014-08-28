package com.tools.h46incon.lockpapercountdown.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import com.tools.h46incon.lockpapercountdown.R;
import com.tools.h46incon.lockpapercountdown.tools.SetWallPaper;
import com.tools.h46incon.lockpapercountdown.tools.UpdateWallPaperReceiver;
import com.tools.h46incon.lockpapercountdown.util.DatePreference;
import com.tools.h46incon.lockpapercountdown.util.GetSPByID;
import com.tools.h46incon.lockpapercountdown.util.ListenDefaultSharedPreferenceChange;
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
		initPreferenceVar();
		initPreference();
		registerPreferenceChangeListeners();
	}

	private void initPreference()
	{
		// disable set lock screen paper option?
		if (SetWallPaper.couldSetLockPaper() == false) {
			// Disable checkbox
			prefUpdateLockPaper.setSummary(getString(R.string.pref_s_couldnot_set_lockpaper_msg));
			prefUpdateLockPaper.setChecked(false);
			prefUpdateLockPaper.setEnabled(false);
			// Disable picture selector
			prefSelectLockPaper.setEnabled(false);
		}
	}

	private void initPreferenceVar()
	{
		prefServiceEnable =
				(SwitchPreference) findPreferenceByID(R.string.pref_key_service_enable);
		prefDestDate =
				(DatePreference) findPreferenceByID(R.string.pref_key_destination_date);

		prefUpdateLockPaper =
				(CheckBoxPreference) findPreferenceByID(R.string.pref_key_is_update_lockpaper);
		prefSelectLockPaper = findPreferenceByID(R.string.pref_keyTag_select_lockpaper);

		prefUpdateWallPaper =
				(CheckBoxPreference) findPreferenceByID(R.string.pref_key_is_update_wallpaper);
		prefSelectWallPaper = findPreferenceByID(R.string.pref_keyTag_select_wallpaper);
	}

	private void registerPreferenceChangeListeners()
	{
		ListenDefaultSharedPreferenceChange.registerListener(
				getString(R.string.pref_key_service_enable),
				new SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
					{
						if (isServiceRunning()) {
							Log.i(TAG, "start service");
							UpdateWallPaperReceiver.startAutoUpdate();
						} else {
							Log.i(TAG, "stop service");
							UpdateWallPaperReceiver.stopAutoUpdate();
						}

					}
				});

		ListenDefaultSharedPreferenceChange.registerListener(
				getString(R.string.pref_key_destination_date),
				new SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
					{
						Log.d(TAG, "Destination date changed!");
						if (isServiceRunning()) {
							SetWallPaper.updatePaper();
						}

					}
				}
		);

		// The common listener to handler both IsUpdate wallpaper or lockpaper setting changed.
		SharedPreferences.OnSharedPreferenceChangeListener updatePaperPrefChangedHandler =
				new SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
					{
						boolean isEnable = sharedPreferences.getBoolean(key, false);
						if (isEnable) {
							Log.d(TAG, "Auto update paper enabled");
							if (isServiceRunning()) {
								// Try to update wallpaper
								if (key.compareTo(
										getString(R.string.pref_key_is_update_wallpaper)) == 0) {
									SetWallPaper.updateWallPaper();
								} else if (key.compareTo(
										getString(R.string.pref_key_is_update_lockpaper)) == 0) {
									SetWallPaper.updateLockPaper();
								} else {
									// error
									Log.e(TAG, "not a updater key: " + key);
								}
							} else {
								Log.d(TAG, "Will not update paper cause service is not running");
								MyApplication.showSimpleToast(getString(R.string.msg_service_not_running));
							}
						}
					}
				};

		ListenDefaultSharedPreferenceChange.registerListener(
				getString(R.string.pref_key_is_update_wallpaper),
				updatePaperPrefChangedHandler
		);

		ListenDefaultSharedPreferenceChange.registerListener(
				getString(R.string.pref_key_is_update_lockpaper),
				updatePaperPrefChangedHandler
		);
	}

	private Preference findPreferenceByID(int stringID)
	{
		String keyName = getString(stringID);
		return findPreference(keyName);
	}

	private boolean isServiceRunning()
	{
		return GetSPByID.getBoolean(R.string.pref_key_service_enable, false);
	}


	private static final String TAG = "SettingFragment";

	// These variable will be init in initPreferenceVar()
	private SwitchPreference prefServiceEnable;
	private DatePreference prefDestDate;
	private CheckBoxPreference prefUpdateLockPaper;
	private Preference prefSelectLockPaper;
	private CheckBoxPreference prefUpdateWallPaper;
	private Preference prefSelectWallPaper;

}
