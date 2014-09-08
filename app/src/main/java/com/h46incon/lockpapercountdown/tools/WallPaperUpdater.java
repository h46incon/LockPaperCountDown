package com.h46incon.lockpapercountdown.tools;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.h46incon.lockpapercountdown.R;
import com.h46incon.lockpapercountdown.util.GetSPByID;
import com.h46incon.lockpapercountdown.util.MyApp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

	public static class TextParam implements Serializable{
		public String toBase64()
		{
			try {
				ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
				ObjectOutputStream objectOS = new ObjectOutputStream(byteOS);
				objectOS.writeObject(this);
				return Base64.encodeToString(byteOS.toByteArray(), Base64.DEFAULT);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		public static TextParam fromBase64(final String str)
		{
			byte[] bytes = Base64.decode(str, Base64.DEFAULT);
			ByteArrayInputStream byteIS = new ByteArrayInputStream(bytes);
			try {
				ObjectInputStream objetcIS = new ObjectInputStream(byteIS);
				return (TextParam) objetcIS.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			return null;
		}

		public String fontPath; //null or "" to set Default font type
		public float textSize;  // sp, may be change to px later.
		public float xCenter;
		public float baseLine;
		public int color;
	}

	public static boolean couldSetLockPaper()
	{
		Method setLockPaperMethod = tryGetSetLockPaperMethod();
		return setLockPaperMethod != null;
	}

	public static Size getDefaultWallPaperSize()
	{
		Size size = new Size();
		size.weight = mWallManager.getDesiredMinimumWidth();
		size.height = mWallManager.getDesiredMinimumHeight();

		if (size.weight != 0 && size.height != 0) {
			return size;
		} else {
			return getRollingWallPaperSize();
		}
	}

	public static Size getFixedWallPaperSize()
	{
		return getScreenSize();
	}

	public static Size getRollingWallPaperSize()
	{
		Size size = getScreenSize();
		size.weight *= 2;
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
			size = getScreenSize();
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

		TextParam textParam = readTextParam(lockTextParamSPKey);
		if (textParam == null) {
			textParam = createDefaultTextParam(template);
		}

		Bitmap lockPaper = drawTextInBitmap(template, "" + countDownNumber, textParam);

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
			template = createEmptyBitmap(getDefaultWallPaperSize());
		}

		TextParam textParam = readTextParam(wallTextParamSPKey);
		if (textParam == null) {
			textParam = createDefaultTextParam(template);
		}

		Bitmap newb = drawTextInBitmap(template, "" + countDownNumber, textParam);

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

	public static void setWallPaperTextParam(TextParam param)
	{
		storeTextParam(param, wallTextParamSPKey);
	}

	/*
		Note: It will move {@param imageFile} !
	 */
	public static boolean setLockPaperTemplate(File imageFile)
	{
		File outFile = new File(appCont.getFilesDir(), lockPaperFileName);
//		outFile.delete();
		return imageFile.renameTo(outFile);
	}

	public static void setLockPaperTextParam(TextParam param)
	{
		storeTextParam(param, lockTextParamSPKey);
	}

	//================private
	private static void storeTextParam(TextParam param, String key)
	{
		String base64 =param.toBase64();
		SharedPreferences.Editor edit = defaultSP.edit();
		edit.putString(key, base64);
		edit.commit();
	}

	private static TextParam readTextParam(String key)
	{
		String str = defaultSP.getString(key, null);
		if (str == null) {
			return null;
		}

		return TextParam.fromBase64(str);
	}

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

	private static Size getScreenSize()
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

	private static Bitmap drawTextInBitmap(Bitmap template, String msgToDraw, TextParam param)
	{
		Canvas canvasTmp = new Canvas(template);

		Paint p = new Paint();
		Typeface font = Typeface.DEFAULT;
		if (param.fontPath != null
				&& !param.fontPath.isEmpty()) {
			font = Typeface.createFromFile(param.fontPath);
		}

		p.setColor(param.color);
		p.setTypeface(font);
		p.setTextSize(param.textSize);
		p.setTextAlign(Paint.Align.CENTER);

		canvasTmp.drawText(msgToDraw, param.xCenter, param.baseLine, p);
		canvasTmp.save(Canvas.ALL_SAVE_FLAG);
		canvasTmp.restore();
		return template;
	}

	private static TextParam createDefaultTextParam(Bitmap template)
	{
		TextParam param = new TextParam();
		param.fontPath = null;
		param.color = Color.RED;
		param.baseLine = template.getHeight() / 2;
		param.xCenter = template.getWidth() / 2;
		// TODO:
		param.textSize = 100f;

		return param;
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

	private static WallpaperManager mWallManager =
			WallpaperManager.getInstance(MyApp.getContext());
	private static Context appCont = MyApp.getContext();
	private static SharedPreferences defaultSP = MyApp.getSharedPreferences();

	private static final Bitmap.Config mBitMapConf = Bitmap.Config.ARGB_8888;
	private static final String TAG = "SetWallPaper";
	private static final String wallPaperFileName = "HomeScreen.jpg";
	private static final String lockPaperFileName = "LockScreen.jpg";
	private static final String wallTextParamSPKey =
			appCont.getString(R.string.pref_key_wallpaper_text_param);
	private static final String lockTextParamSPKey =
			appCont.getString(R.string.pref_key_lockpaper_text_param);


}
