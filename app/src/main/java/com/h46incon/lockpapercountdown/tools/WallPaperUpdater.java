package com.h46incon.lockpapercountdown.tools;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.h46incon.lockpapercountdown.R;
import com.h46incon.lockpapercountdown.util.GetSPByID;
import com.h46incon.lockpapercountdown.util.MyApp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by h46incon on 2014/8/26.
 */
public class WallPaperUpdater {
	public static class Size{
		public int weight;
		public int height;
	}

	public static boolean couldSetLockPaper()
	{
		Method setLockPaperMethod = tryGetSetLockPaperMethod();
		return setLockPaperMethod != null;
	}

	public static Size getWallPaperSize()
	{
		Size size = new Size();
		size.weight = mWallManager.getDesiredMinimumWidth();
		size.height = mWallManager.getDesiredMinimumHeight();
		return size;
	}

	/*
		Try return Lock Screen Paper Size according to API in MeiZu FlyMe OS first
		or return size of screen in other ROMs
	 */
	public static Size getLockPaperSize()
	{
		Size size = new Size();
		boolean hasGetSize = false;
		Class<?> wallPaperMangerClass = WallpaperManager.class;

		// Try get size from API
		try {
			Method getLockWallpaperDesiredMinimumWidthMethod =
					wallPaperMangerClass.getMethod("getLockWallpaperDesiredMinimumWidth");
			// It's just getlock... Not getLock...
			Method getLockWallpaperDesiredMinimumHeightMethod =
					wallPaperMangerClass.getMethod("getlockWallpaperDesiredMinimumHeight");

			size.weight = (Integer) getLockWallpaperDesiredMinimumWidthMethod.invoke(mWallManager);
			size.height = (Integer) getLockWallpaperDesiredMinimumHeightMethod.invoke(mWallManager);
			hasGetSize = true;
		} catch (IllegalAccessException ignored) {
		} catch (InvocationTargetException ignored) {
		} catch (NoSuchMethodException ignored) {
		}

		// Not successful, return size of screen
		if (!hasGetSize) {
			size = getScreenPixels();
		}

		return size;
	}

	/*
		update wallpaper or lock screen wallpaper according to shared preference's setting
	 */
	public static boolean updatePaper()
	{
		boolean isSuccess  = true;
		boolean needUpdateWallPaper =
				GetSPByID.getBoolean(R.string.pref_key_is_update_wallpaper, false);
		boolean needUpdateLockPaper =
				GetSPByID.getBoolean(R.string.pref_key_is_update_lockpaper, false);

		if (needUpdateLockPaper) {
			isSuccess &= updateLockPaper();
		}
		if (needUpdateWallPaper) {
			isSuccess &= updateWallPaper();
		}

		return isSuccess;
	}

	/*
		Warning: The function is only available for MeiZu phone, MX3 tested
		Update lock screen's count down number
	 */
	public static boolean updateLockPaper()
	{
		Method setLockPaperMethod = tryGetSetLockPaperMethod();

		if (setLockPaperMethod == null) {
			Log.e(TAG, "Could not get method 'setBitmapToLockWallPaper'");
			return false;
		}

		int countDownNumber = getCountDownNumber();
		Bitmap template = decodeStoredImage(lockPaperFileName);
		if (template == null) {
			template = createEmptyBitmap(getLockPaperSize());
		}
		Bitmap lockPaper = drawTextInBitmap(template, "" + countDownNumber, 700, 1520);

		try {
			setLockPaperMethod.invoke(mWallManager, lockPaper);
		} catch (IllegalAccessException e) {
			e.getCause().printStackTrace();
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean updateWallPaper()
	{
		int countDownNumber = getCountDownNumber();
		Bitmap template = decodeStoredImage(wallPaperFileName);
		if (template == null) {
			template = createEmptyBitmap(getWallPaperSize());
		}
		Bitmap newb = drawTextInBitmap(template, "" + countDownNumber, 500, 800);

		try {
			mWallManager.setBitmap(newb);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
		Note: It will move {@param imageFile} !
	 */
	public static boolean setWallPaperTemplate(File imageFile)
	{
		File outFile = new File(appCont.getFilesDir(), wallPaperFileName);
//		outFile.delete();
		return imageFile.renameTo(outFile);
	}


	//================private
	private static Method tryGetSetLockPaperMethod()
	{
		Class<?> wallPaperMangerClass = WallpaperManager.class;
		try {
			Method setLockPaperMethod =
					wallPaperMangerClass.getDeclaredMethod("setBitmapToLockWallpaper", Bitmap.class);
			setLockPaperMethod.setAccessible(true);
			return setLockPaperMethod;

		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	private static Size getScreenPixels()
	{
		WindowManager windowManager =
				(WindowManager) appCont.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();

		Size size = new Size();
		// For JellyBeans and onward
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			DisplayMetrics metrics = new DisplayMetrics();
			display.getRealMetrics(metrics);
			size.weight = metrics.widthPixels;
			size.height = metrics.heightPixels;
		} else {
			try {
				Method mGetRawH = Display.class.getMethod("getRawHeight");
				Method mGetRawW = Display.class.getMethod("getRawWidth");
				size.weight = (Integer) mGetRawW.invoke(display);
				size.height = (Integer) mGetRawH.invoke(display);
			} catch (IllegalArgumentException ignored) {
			} catch (IllegalAccessException ignored) {
			} catch (InvocationTargetException ignored) {
			} catch (NoSuchMethodException ignored) {
			}
		}

		return size;
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

	private static Bitmap drawTextInBitmap(Bitmap template, String msgToDraw, float x, float y)
	{
		Canvas canvasTmp = new Canvas(template);
		canvasTmp.drawColor(Color.TRANSPARENT);

		Paint p = new Paint();
		Typeface font = Typeface.create("Arial", Typeface.BOLD);
		p.setColor(Color.RED);
		p.setTypeface(font);
		p.setTextSize(150);
		canvasTmp.drawText(msgToDraw, x, y, p);
		canvasTmp.save(Canvas.ALL_SAVE_FLAG);
		canvasTmp.restore();
		return template;
	}

	private static Bitmap createEmptyBitmap( Size emptySize)
	{
		return Bitmap.createBitmap(emptySize.weight, emptySize.height, mBitMapConf);
	}

	private static Bitmap decodeStoredImage(String imgName)
	{
		File templateFile = getStoredFile(imgName);

		if (templateFile.canRead()) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			Bitmap templateBM = BitmapFactory.decodeFile(templateFile.getPath(), options);
			Log.d(TAG, "TemplateBitMap size : " + templateBM.getWidth() + " * " + templateBM.getHeight());

			// new bitmap read before is immutable
			Bitmap newb = templateBM.copy(mBitMapConf, true);
			templateBM.recycle();
			return newb;
		}

		return null;
	}

	private static File getStoredFile(String fileName)
	{
		return new File(appCont.getFilesDir(), fileName);
	}

	private static final Bitmap.Config mBitMapConf = Bitmap.Config.ARGB_8888;
	private static final String TAG = "SetWallPaper";
	private static final String wallPaperFileName = "HomeScreen.jpg";
	private static final String lockPaperFileName = "LockScreen.jpg";

	private static WallpaperManager mWallManager =
			WallpaperManager.getInstance(MyApp.getContext());
	private static Context appCont = MyApp.getContext();

}
