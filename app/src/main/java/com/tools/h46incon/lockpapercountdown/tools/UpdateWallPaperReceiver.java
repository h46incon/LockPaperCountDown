package com.tools.h46incon.lockpapercountdown.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tools.h46incon.lockpapercountdown.R;
import com.tools.h46incon.lockpapercountdown.util.GetSPByID;
import com.tools.h46incon.lockpapercountdown.util.MyApplication;

import java.util.Calendar;

/**
 * Created by Administrator on 2014/8/26.
 */
public class UpdateWallPaperReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i(TAG, "Receiver a broadcast");

		Log.d(TAG, "update wall paper");
		SetWallPaper.updateWallPaper();

		boolean is_service_running = GetSPByID.getBoolean(R.string.pref_key_service_enable, false);
		if (is_service_running) {
			setNextUpdateAlarm();
		}
	}

	public static void startAutoUpdate()
	{
		SetWallPaper.updateWallPaper();
		setNextUpdateAlarm();
	}

	public static void stopAutoUpdate()
	{
		// TODO:
	}

	private static void setNextUpdateAlarm()
	{

		Intent intent = new Intent(appContext, UpdateWallPaperReceiver.class);
		PendingIntent pendingIntent =
				PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager =
				(AlarmManager) appContext.getSystemService(Service.ALARM_SERVICE);

		// Calc next weekup time
		long nextWeekupTime = tomorrowTimeMillis();
		Log.i(TAG, "setting next alarm: " + nextWeekupTime);
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextWeekupTime, pendingIntent);

	}

	private static long tomorrowTimeMillis()
	{
		Calendar calendar = Calendar.getInstance();
		// Set time to 0:00 for this day
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		// add a day
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Log.d(TAG, "cal date: " + calendar.toString());

		return calendar.getTimeInMillis();
	}

	private static final String TAG = "UpdateReceiver";
	private static Context appContext = MyApplication.getContext();
}
