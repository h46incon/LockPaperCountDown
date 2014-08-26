package com.tools.h46incon.lockpapaercountdown;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.tools.h46incon.lockpapaercountdown.tools.SetWallPaper;

/**
 * Created by Administrator on 2014/8/25.
 */
public class SettingFragment extends PreferenceFragment{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_preference);
		//testSetLockPaper();
		SetWallPaper.testSetWallPaper();
	}

}
