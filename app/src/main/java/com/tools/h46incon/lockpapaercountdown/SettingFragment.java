package com.tools.h46incon.lockpapaercountdown;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.tools.h46incon.lockpapaercountdown.tools.mApplication;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
		testSetWallPaper();
	}

	public static void testSetWallPaper()
	{
		// Test to change lock screen wallpaper
		WallpaperManager mWallManager = WallpaperManager.getInstance(mApplication.getContext());

		Bitmap newb = getWallPaper("test", 500, 800);

		try {
			mWallManager.setBitmap(newb);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		Bitmap templateBM = BitmapFactory.decodeResource(mApplication.getContext().getResources(), R.drawable.lock_screen, options);
		Log.d(TAG, "TemplateBitMap size : " + templateBM.getWidth() + " * " + templateBM.getHeight());

		// new bitmap read before is immutable
		Bitmap newb = templateBM.copy(Bitmap.Config.ARGB_8888, true);

		templateBM.recycle();
		return newb;
	}

	public static void testSetLockPaper()
	{
		WallpaperManager mWallManager = WallpaperManager.getInstance(mApplication.getContext());
		Class<?> wallPaperMangerClass = WallpaperManager.class;
		try {
			Method setLockPaperMethod = wallPaperMangerClass.getDeclaredMethod("setBitmapToLockWallpaper", Bitmap.class);
			setLockPaperMethod.setAccessible(true);

			Bitmap lockPaper = getWallPaper("25", 700, 1520);
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

	private static final String TAG = "SettingFragment";

}
