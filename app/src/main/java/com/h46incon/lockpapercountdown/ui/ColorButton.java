package com.h46incon.lockpapercountdown.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

import com.h46incon.lockpapercountdown.R;

/**
 * Created by Administrator on 2014/9/5.
 */
public class ColorButton extends ImageButton{

	public ColorButton(Context context)
	{
		this(context, null);
	}

	public ColorButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setWillNotDraw(false);

		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ColorButton);
		innerRadius = array.getDimension(R.styleable.ColorButton_innerRadius, Float.NaN);
		innerColor = array.getColor(R.styleable.ColorButton_innerColor, 0xFFFFFFFF);
		array.recycle();

		Log.d(TAG, "Inner Radius: " + innerRadius);
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

//	public ColorButton(Context context, AttributeSet attrs, int defStyle) { }

	public void setInnerColor(int color)
	{
		this.innerColor = color;
		this.invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		mPaint.setColor(innerColor);
//		mPaint.setColor(0xFFFFFFFF);
		float x = getWidth() / 2f;
		float y = getHeight() / 2f;

		float r = innerRadius;
		if (Float.isNaN(r)) {
			r = getWidth() / 4f;
		}
		canvas.drawCircle(x, y, r, mPaint);
	}

	Paint mPaint = new Paint();

	private static final String TAG = "ColorButton";
	private float innerRadius;
	private int innerColor;
}
