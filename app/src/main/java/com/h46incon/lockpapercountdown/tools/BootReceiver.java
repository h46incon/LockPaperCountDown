package com.h46incon.lockpapercountdown.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.h46incon.lockpapercountdown.R;
import com.h46incon.lockpapercountdown.util.GetSPByID;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by h46incon on 2014/8/27.
 * Alarms will be lost after shutdown phone, so need reset the alarm after boot
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent)
	{
		boolean isServiceRunning = GetSPByID.getBoolean(R.string.pref_key_service_enable, false);

		if (isServiceRunning) {
			Log.d(TAG, "LockPaperCountDown's service is running");
			// need to set wall paper alarm
			// Draw a wall paper is a time-consuming operation
			// So start this operation after some time to finish other OnBoot application first
			TimerTask task = new TimerTask() {
				@Override
				public void run()
				{
					UpdateWallPaperReceiver.startAutoUpdate();
				}
			};
			final int waitTimeMillis = 60000;
			(new Timer()).schedule(task, waitTimeMillis);
		} else {
			Log.d(TAG, "LockPaperCountDown's service is not running");
		}
	}

	private final static String TAG = "BootReceiver";
}
