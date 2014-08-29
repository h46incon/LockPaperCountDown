package com.tools.h46incon.lockpapercountdown.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

/**
 * Created by Administrator on 2014/8/28.
 */
public class ListenDefaultSharedPreferenceChange {
	public static void registerListener(String key, OnSharedPreferenceChangeListener listener)
	{
		List<OnSharedPreferenceChangeListener> listenerList =
				keyHandlerMap.get(key);

		if (listenerList == null) {
			listenerList = new LinkedList<OnSharedPreferenceChangeListener>();
			keyHandlerMap.put(key, listenerList);
		}

		listenerList.add(listener);
	}

	public static boolean unregisterListener(String key, OnSharedPreferenceChangeListener listener)
	{
		List<OnSharedPreferenceChangeListener> listenerList =
				keyHandlerMap.get(key);

		if (listenerList == null) {
			return false;
		} else {
			return listenerList.remove(listener);
		}
	}

	//=============== private
	private ListenDefaultSharedPreferenceChange() { }

	private static Context appCont = MyApplication.getContext();
	private static HashMap<String, List<OnSharedPreferenceChangeListener>> keyHandlerMap =
			new HashMap<String, List<OnSharedPreferenceChangeListener>>();

	private static SharedPreferences sharedPreferences =
			MyApplication.getSharedPreferences();

	private static OnSharedPreferenceChangeListener mListener =
			new OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
				{
					List<OnSharedPreferenceChangeListener> listeners =
							keyHandlerMap.get(key);
					if (listeners != null) {
						for (OnSharedPreferenceChangeListener listener : listeners) {
							listener.onSharedPreferenceChanged(sharedPreferences, key);
						}
					}
				}
			};

	static{
		sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
	}
}
