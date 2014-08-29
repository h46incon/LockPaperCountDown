package com.h46incon.lockpapercountdown.util;

import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.preference.Preference;

/**
 * Created by Administrator on 2014/4/24.
 */
public class AutoUpdatePrefSummary {
	public static interface GetPrefMsgCB{
		public String onGetMeg(Preference pref);
	}

	public static boolean addAutoUpdate(Preference pref, final String template, final GetPrefMsgCB cb)
	{
		if (pref != null) {
			setPrefSummary(pref, template, cb);

			pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(final Preference preference, Object newVal)
				{
					// The lPref's entry will not change before this callback return true.
					// So need to change the summary in next message loop to get the correct entry.
					UIHandler.post(new Runnable() {
						@Override
						public void run()
						{
							setPrefSummary(preference, template, cb);
						}
					});
					return true;
				}
			});
			return true;
		} else {
			return false;
		}
	}

	public static boolean addAutoUpdate(Preference pref, final int templateID, final GetPrefMsgCB cb)
	{
		String template;
		if (templateID == 0) {
			template = null;
		} else {
			template = MyApplication.getContext().getString(templateID);
		}

		return addAutoUpdate(pref, template, cb);
	}

	public static String getPrefMsgToShown(Preference pref)
	{
		String msg = "";

		if (pref instanceof ListPreference) {
			ListPreference lpref = (ListPreference) pref;

			try {
				msg = lpref.getEntry().toString();
			} catch (NullPointerException e) {
				msg = "";
			}
		} else {
			String key = pref.getKey();
			msg = MyApplication.getSharedPreferences().getString(key, "");
		}

		return msg;
	}


	//========== private
	private static void setPrefSummary(Preference pref, String template, GetPrefMsgCB getPrefMsgCB)
	{
		String msgToShow;
		if (getPrefMsgCB == null) {
			msgToShow = getPrefMsgToShown(pref);
		} else {
			msgToShow = getPrefMsgCB.onGetMeg(pref);
		}

		String summary;
		if (template == null) {
			summary = msgToShow;
		} else {
			summary = String.format(template, msgToShow);
		}

		pref.setSummary(summary);
	}


	private final static Handler UIHandler = new Handler(Looper.getMainLooper());
}
