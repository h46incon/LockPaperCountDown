package com.tools.h46incon.lockpapaercountdown.tools;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import com.tools.h46incon.lockpapaercountdown.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Administrator on 2014/8/25.
 */
public class DatePreference extends DialogPreference{
	public DatePreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.date_preference);

	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
	{
		if (restorePersistedValue) {
			long defaultTime = System.currentTimeMillis();
			mDateInMS = getPersistedLong(defaultTime);
		} else {
			mDateInMS = Long.parseLong((String) defaultValue);
			persistLong(mDateInMS);
		}

	}

	@Override
	protected void onBindDialogView(View view)
	{
		super.onBindDialogView(view);
		mDatePicker = (DatePicker) view.findViewById(R.id.datePicker_preference);
		if (mDatePicker != null) {
			// Init DatePicker
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mDateInMS);

			mDatePicker.init(
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH),
					null);
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		if (positiveResult) {
			Calendar calendar =
					new GregorianCalendar(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
			mDateInMS = calendar.getTimeInMillis();
			persistLong(mDateInMS);
		}
	}

	private DatePicker mDatePicker;
	private long mDateInMS;
}
