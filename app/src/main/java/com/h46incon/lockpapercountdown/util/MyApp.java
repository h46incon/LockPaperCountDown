package com.h46incon.lockpapercountdown.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2014/4/11.
 */
public class MyApp extends Application {

	public static void addOnAppInitCallBack(Runnable runnable)
	{
		onAppInitCB.add(runnable);
		// if onCreate has been run, then run the callback here.
		if (hasAppInited) {
			runnable.run();
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		applicationContext = getApplicationContext();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (!hasAppInited) {
			for (Runnable runnable : onAppInitCB) {
				if (runnable != null) {
					runnable.run();
				}
			}
		}
		hasAppInited = true;
	}

	public static void showSimpleToast(CharSequence msg)
	{
		Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
	}

	public static boolean isServiceRunning(String serviceClassName)
	{
		ActivityManager activityManager =
				(ActivityManager)instance.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> infoList
				= activityManager.getRunningServices(Integer.MAX_VALUE);

		for (ActivityManager.RunningServiceInfo info : infoList) {
			if (info.service.getClassName().equals(serviceClassName)) {
				return true;
			}
		}
		return false;
	}

	public static Context getContext()
	{
		return applicationContext;
	}

	public static SharedPreferences getSharedPreferences()
	{
		return sharedPreferences;
	}

	private static MyApp instance;
	private static Context applicationContext;
	private static SharedPreferences sharedPreferences;

	private static Collection<Runnable> onAppInitCB = new LinkedList<Runnable>();
	private static boolean hasAppInited = false;
}
