package com.tools.h46incon.lockpapaercountdown.tools;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.tools.h46incon.lockpapaercountdown.R;
import com.tools.h46incon.lockpapaercountdown.util.GetSPByID;
import com.tools.h46incon.lockpapaercountdown.util.MyApplication;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2014/8/26.
 */
public class SetWallPaper{
	/*
		Warning: The function is only available for MeiZu phone, MX3 tested
		Update lock screen's count down number
	 */
	public static void updateLockPaper()
	{
		WallpaperManager mWallManager = WallpaperManager.getInstance(MyApplication.getContext());
		Class<?> wallPaperMangerClass = WallpaperManager.class;
		try {
			Method setLockPaperMethod = wallPaperMangerClass.getDeclaredMethod("setBitmapToLockWallpaper", Bitmap.class);
			setLockPaperMethod.setAccessible(true);

			int countDownNumber = getCountDownNumber();
			Bitmap lockPaper = getWallPaper("" + countDownNumber, 700, 1520);
//			Method getLockWallpaperDesiredMinimumWidthMethod = wallPaperMangerClass.getMethod("getLockWallpaperDesiredMinimumWidth");
//			int minWidth = (Integer)getLockWallpaperDesiredMinimumWidthMethod.invoke(mWallManager);
//			Method getLockWallpaperDesiredMinimumHeightMethod = wallPaperMangerClass.getMethod("getlockWallpaperDesiredMinimumHeight");
//			int minHeight = (Integer)getLockWallpaperDesiredMinimumHeightMethod.invoke(mWallManager);

			setLockPaperMethod.invoke(mWallManager, lockPaper);

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void updateWallPaper()
	{
		// Test to change lock screen wallpaper
		WallpaperManager mWallManager = WallpaperManager.getInstance(MyApplication.getContext());

		int countDownNumber = getCountDownNumber();
		Bitmap newb = getWallPaper("" + countDownNumber, 500, 800);

		try {
			mWallManager.setBitmap(newb);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int getCountDownNumber()
	{
		long desTime = GetSPByID.getLong(R.string.pref_key_destination_date, -1);
		if (desTime == -1) {
			return -1;
		}

		long curTime = System.currentTimeMillis();
		long disTimeInMillis = desTime - curTime;

		final long millisOfDay = 1000 * 60 * 60 * 24;

		// The destination time must be a date which time is 0:00
		return (int)(disTimeInMillis / millisOfDay) + 1;
	}

	private static Bitmap getWallPaper(String msgToDraw, float x, float y)
	{
		Bitmap newb = getWallpaperTemplate();

		Canvas canvasTmp = new Canvas(newb);
		canvasTmp.drawColor(Color.TRANSPARENT);

		Paint p = new Paint();
		Typeface font = Typeface.create("Arial", Typeface.BOLD);
		p.setColor(Color.RED);
		p.setTypeface(font);
		p.setTextSize(150);
		canvasTmp.drawText(msgToDraw, x, y, p);
		canvasTmp.save(Canvas.ALL_SAVE_FLAG);
		canvasTmp.restore();
		return newb;
	}

	private static Bitmap getWallpaperTemplate()
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		Bitmap templateBM = BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.lock_screen, options);
		Log.d(TAG, "TemplateBitMap size : " + templateBM.getWidth() + " * " + templateBM.getHeight());

		// new bitmap read before is immutable
		Bitmap newb = templateBM.copy(Bitmap.Config.ARGB_8888, true);

		templateBM.recycle();
		return newb;
	}

	private static final String TAG = "SetWallPaper";

}
