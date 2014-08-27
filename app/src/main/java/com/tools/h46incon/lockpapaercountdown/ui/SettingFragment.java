package com.tools.h46incon.lockpapaercountdown.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.tools.h46incon.lockpapaercountdown.R;
import com.tools.h46incon.lockpapaercountdown.tools.UpdateWallPaperReceiver;
import com.tools.h46incon.lockpapaercountdown.util.MyApplication;


/**
 * Created by Administrator on 2014/8/25.
 */
public class SettingFragment extends PreferenceFragment{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_preference);
		listenSP();
	}

	private void listenSP()
	{
		SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(
				listener
		);
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
