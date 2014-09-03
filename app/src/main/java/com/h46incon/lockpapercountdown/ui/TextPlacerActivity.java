package com.h46incon.lockpapercountdown.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.h46incon.lockpapercountdown.R;
import com.h46incon.lockpapercountdown.ui.fontpicker.FontPickerDialog;
import com.soundcloud.android.crop.CropImageView;
import com.soundcloud.android.crop.HighlightView;
import com.soundcloud.android.crop.ImageAreaPickerActivity;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by h46incon on 2014/8/29.
 */
public class TextPlacerActivity extends ImageAreaPickerActivity implements FontPickerDialog.FontPickerDialogListener{
	public static interface Extra{
		public final String FontPath = "__font_path";
		public final String Color = "__color";
		public final String FontSize = "__font_size";
		public final String FontCenterX = "__font_center_x";
		public final String FontBaseLine = "__font_base_line";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.acivity_textplacer);

		if (rotateBitmap == null) {
			return;
		}

		initViewVar();
		initListener();

		restartPicker();
	}

	private void restartPicker()
	{
		stopPicker();
		initTextBoundsReference();
		setupPicker();
		startPicker();
	}

	private void setupPicker()
	{
		Rect selectedArea = getImageSelectedArea();
		RectF initArea;

		if (selectedArea == null) {
			initArea = getInitSelectArea(0.5f);
		} else {
			// change width to fill new font
			float scale = (float) selectedArea.height() / (float) textRefBound.height();
			float widthTarget = (float) textRefBound.width() * scale;
			// new a target rect
			Rect newImageA = new Rect(0, 0, (int) widthTarget, selectedArea.height());
			// move
			newImageA.offset(
					selectedArea.centerX() - newImageA.centerX(),
					selectedArea.centerY() - newImageA.centerY()
			);

			// Change to percent rectF
			float rawWidth = (float) this.getImageWidth();
			float rawHeight = (float) this.getImageHeight();
			initArea = new RectF(
					(float) newImageA.left / rawWidth,
					(float) newImageA.top / rawHeight,
					(float) newImageA.right / rawWidth,
					(float) newImageA.bottom / rawWidth
			);
		}

		// Set up picker view
		setupPickerView(imageView, initArea, true);

		HighlightView v = getPickerView();
		v.setShowThirds(false);
		v.setNeedCenterBaseOnThis(false);
		v.setEnsureVisable(false);
		v.setMustInsideImage(false);

		v.setOnDrawFinshed(new HighlightView.OnDrawFinished() {
			@Override
			public void onDrawFinished(HighlightView highlightView, Canvas canvas)
			{
				RectF viewArea = highlightView.getCropRectOnScreen();
				drawTextInRect(canvas, viewArea);
			}
		});

	}

	@Override
	protected void setResultException(Throwable throwable)
	{
		return;
	}

	@Override
	protected boolean respondTouchEvent()
	{
		return true;
	}

	private RectF getInitSelectArea(float scaleSize)
	{
		// get aspect ration of text to show
		float ratio = (float) textRefBound.height() / (float) textRefBound.width();

		// get unscaled height and width
		float h;
		float w;
		if (ratio > 1) {
			h = 1;
			w = h / ratio;
		} else {
			w = 1;
			h = w * ratio;
		}

		// scale according to scale size
		h *= scaleSize;
		w *= scaleSize;
		// scale according bitmap aspect ratio
		float bmRatio = (float) rotateBitmap.getHeight() / (float) rotateBitmap.getWidth();
		// now h:w == 1:1, need set h:w = 1/(bmRatio:1) = 1:bmRatio
		if (bmRatio > 1) {
			h /= bmRatio;
		} else {
			w *= bmRatio;
		}

		// Init result rect
		RectF rect = new RectF(0,0,w,h);
		// center it
		rect.offset(
				0.5f - rect.centerX(),
				0.5f - rect.centerY()
				);

		return rect;
	}

	private void initTextBoundsReference()
	{
		textRefSize = 2048f;
		Rect bound = new Rect();
		Paint paint = new Paint();
		paint.setTypeface(mTypeface);
		paint.setTextSize(textRefSize);
		paint.getTextBounds(textToShow, 0, textToShow.length(), bound);
		this.textRefBound = bound;
	}

	private void initViewVar()
	{
		pickColorBtn = (Button) findViewById(R.id.pick_color_btn);
		pickFontBtn = (Button) findViewById(R.id.pick_font_btn);
		imageView = (CropImageView) findViewById(R.id.placer_image_view);
		downCancelBar = findViewById(R.id.done_cancel_bar);
		downBtn = downCancelBar.findViewById(R.id.btn_done);
		cancelBtn =  downCancelBar.findViewById(R.id.btn_cancel);
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

		pickFontBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				mFontPickerDialog.show(getFragmentManager(), "NO_MEANING_TAG");
			}
		});

		downBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				Intent intent = makeResultIntent();
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	@Override
	public void onFontSelected(FontPickerDialog dialog)
	{
		String selectedFont = dialog.getSelectedFont();
		Log.d(TAG, "Selected Font: " + selectedFont);
		fontPath = selectedFont;
		mTypeface = Typeface.createFromFile(selectedFont);
		restartPicker();
	}

	private void drawTextInRect(Canvas canvas, RectF rectF)
	{
		FontInfo info = fillFontInRect(rectF);
		Paint paint = new Paint();
		paint.setTypeface(mTypeface);
		paint.setColor(selectedColor);
		paint.setTextSize(info.fontSize);
		paint.setTextAlign(textAlign);

		canvas.drawText(textToShow, info.xCenter, info.baseLine, paint);
	}

	private FontInfo fillFontInRect(RectF rect)
	{
		Paint paint = new Paint();

		float scaleSize = rect.height() / (float) textRefBound.height();
		float fontSize = textRefSize * scaleSize;

		paint.setTypeface(mTypeface);
		paint.setTextSize(fontSize);
		paint.setTextAlign(textAlign);
		paint.setTextScaleX(1f);

		Rect r = new Rect();
		paint.getTextBounds(textToShow, 0, textToShow.length(), r);

		FontInfo info = new FontInfo();
		info.fontSize = fontSize;
		info.baseLine = rect.bottom - r.bottom;
		info.xCenter = rect.centerX();

		return info;
	}

	private Intent makeResultIntent()
	{
		Rect selectedArea = getImageSelectedArea();
		FontInfo info = fillFontInRect(new RectF(selectedArea));

		Intent intent = new Intent();

		intent.putExtra(Extra.FontPath, fontPath);
		intent.putExtra(Extra.Color, selectedColor);
		intent.putExtra(Extra.FontSize, info.fontSize);
		intent.putExtra(Extra.FontCenterX, info.xCenter);
		intent.putExtra(Extra.FontBaseLine, info.baseLine);
		intent.setData(sourceUri);

		return intent;
	}

	private static class FontInfo {
		public float xCenter = Float.NaN;
		public float baseLine = Float.NaN;
		public float fontSize = Float.NaN;
	}

	private AmbilWarnaDialog.OnAmbilWarnaListener mOnColorSelected =
			new AmbilWarnaDialog.OnAmbilWarnaListener() {
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
			imageView.invalidate();
		}
	};

	FontPickerDialog mFontPickerDialog = new FontPickerDialog();

	Button pickColorBtn;
	Button pickFontBtn;
	View downCancelBar;
	View downBtn;
	View cancelBtn;

	private Paint.Align textAlign = Paint.Align.CENTER;
	private Typeface mTypeface = Typeface.DEFAULT;
	private String fontPath = "";
	private float textRefSize;
	private Rect textRefBound;
	private String textToShow = "text-g";

	private CropImageView imageView;
	private final String TAG = "TextPlacerActivity";
	private int selectedColor = 0xFFFF0000;     // Init selected color as RED

}
