package com.h46incon.lockpapercountdown.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.h46incon.lockpapercountdown.R;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by h46incon on 2014/8/29.
 */
public class TextPlacerActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.acivity_textplacer);
		initViewVar();
		initListener();

	}

	private void initViewVar()
	{
		pickColorBtn = (Button) findViewById(R.id.pick_color_btn);
		pickFontBtn = (Button) findViewById(R.id.pick_font_btn);
	}

	private void initListener()
	{
		pickColorBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				AmbilWarnaDialog ambilWarnaDialog =
						new AmbilWarnaDialog(TextPlacerActivity.this, selectedColor, true, mOnColorSelected);
				ambilWarnaDialog.show();
			}
		});
	}


	private AmbilWarnaDialog.OnAmbilWarnaListener mOnColorSelected = new AmbilWarnaDialog.OnAmbilWarnaListener() {
		@Override
		public void onCancel(AmbilWarnaDialog dialog)
		{
			Log.d(TAG, "color picker dialog is cancel");
		}

		@Override
		public void onOk(AmbilWarnaDialog dialog, int color)
		{
			selectedColor = color;
			Log.d(TAG, "Select color" + color);
		}
	};

	Button pickColorBtn;
	Button pickFontBtn;

	private final String TAG = "TextPlacerActivity";
	private int selectedColor = 0xFFFF0000;     // Init selected color as RED
}
