package com.tools.h46incon.lockpapercountdown.ui;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Administrator on 2014/8/25.
 */
public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingFragment())
				.commit();
	}
}
