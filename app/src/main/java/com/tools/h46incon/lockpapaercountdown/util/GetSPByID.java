package com.tools.h46incon.lockpapaercountdown.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by h46incon on 2014/4/13.
 */
public class GetSPByID{
	public static String getString(int id, String def)
	{
		return sp.getString(context.getString(id), def);
	}

	public static Set<String> getStringSet(int id, Set<String> def)
	{
		return sp.getStringSet(context.getString(id), def);
	}

	public static int getInt(int id, int def)
	{
		return sp.getInt(context.getString(id), def);
	}

	public static long getLong(int id, long def)
	{
		return sp.getLong(context.getString(id), def);
	}

	public static float getFloat(int id, float def)
	{
		return sp.getFloat(context.getString(id), def);
	}

	public static boolean getBoolean(int id, boolean def)
	{
		return sp.getBoolean(context.getString(id), def);
	}

	public static boolean contains(int id)
	{
		return sp.contains(context.getString(id));
	}


	//=== field
	private static SharedPreferences sp;
	private static Context context;
	// Init Static field
	static {
		mApplication.addOnAppInitCallBack(new Runnable() {
			@Override
			public void run()
			{
				sp = mApplication.getSharedPreferences();
				context = mApplication.getContext();
			}
		});
	}

}
