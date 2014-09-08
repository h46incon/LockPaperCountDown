package com.h46incon.lockpapercountdown.tools;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.h46incon.lockpapercountdown.ui.TextPlacerActivity;
import com.h46incon.lockpapercountdown.util.MyApp;
import com.soundcloud.android.crop.Crop;

import java.io.File;

/**
 * Created by Administrator on 2014/9/8.
 */
public class WallPaperPicker {
	public static enum PaperType{
		WALL_PAPER,
		LOCK_PAPER
	}

	public WallPaperPicker(Activity activity)
	{
		mActivity = activity;
		mFragment = null;
	}

	public WallPaperPicker(Fragment fragment)
	{
		mActivity = null;
		mFragment = fragment;
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode) {
			case ID_SEL_WALLPAPER:
				onImageSelected(resultCode, data,
						WallPaperUpdater.getDefaultWallPaperSize(), ID_CROP_WALLPAPER);
				break;

			case ID_SEL_LOCKPAPER:
				onImageSelected(resultCode, data,
						WallPaperUpdater.getLockPaperSize(), ID_CROP_LOCKPAPER);
				break;

			case ID_CROP_WALLPAPER:
				onImageCropped(resultCode, data, ID_PLACER_WALLPAPER);
				break;

			case ID_CROP_LOCKPAPER:
				onImageCropped(resultCode, data, ID_PLACER_LOCKPAPER);
				break;

			case ID_PLACER_WALLPAPER:
			case ID_PLACER_LOCKPAPER:
				if (resultCode == Activity.RESULT_OK) {
					Log.d(TAG, "placer ok");
					File imgFile = new File(data.getData().getPath());
					WallPaperUpdater.TextParam param = getTextParamFromIntent(data);

					if (requestCode == ID_PLACER_WALLPAPER) {
						WallPaperUpdater.setWallPaperTextParam(param);
						WallPaperUpdater.setWallPaperTemplate(imgFile);
						// TODO: Check service running state
						WallPaperUpdater.updateWallPaper();
					} else {
						WallPaperUpdater.setLockPaperTextParam(param);
						WallPaperUpdater.setLockPaperTemplate(imgFile);
						// TODO: Check service running state
						WallPaperUpdater.updateLockPaper();
					}
				}
				break;

			default:
				return false;
		}
		return true;
	}

	public void startFromPicker(PaperType type)
	{
		switch (type) {
			case WALL_PAPER:
				selectPaper(ID_SEL_WALLPAPER);
				break;

			case LOCK_PAPER:
				selectPaper(ID_SEL_LOCKPAPER);
				break;

			default:
				break;
		}
	}

	public void startFromCropper(PaperType type, Uri input)
	{
		switch (type) {
			case WALL_PAPER:
				cropImage(input, WallPaperUpdater.getDefaultWallPaperSize(), ID_CROP_WALLPAPER);
				break;

			case LOCK_PAPER:
				cropImage(input, WallPaperUpdater.getLockPaperSize(), ID_CROP_LOCKPAPER);
				break;

			default:
				break;
		}
	}


	private WallPaperUpdater.TextParam getTextParamFromIntent(Intent data)
	{
		WallPaperUpdater.TextParam param = new WallPaperUpdater.TextParam();
		param.textSize = data.getFloatExtra(TextPlacerActivity.Extra.FontSize, 0f);
		param.fontPath = data.getStringExtra(TextPlacerActivity.Extra.FontPath);
		param.xCenter = data.getFloatExtra(TextPlacerActivity.Extra.FontCenterX, 0f);
		param.baseLine = data.getFloatExtra(TextPlacerActivity.Extra.FontBaseLine, 0f);
		param.color = data.getIntExtra(TextPlacerActivity.Extra.Color, 0);
		return param;
	}

	private void onImageCropped(int resultCode, Intent data, int nextStepID)
	{
		if (resultCode == Activity.RESULT_OK) {
			Uri out = Crop.getOutput(data);
			Intent intent = new Intent(MyApp.getContext(), TextPlacerActivity.class);
			intent.setData(out);
			startActivityForResult(intent, nextStepID);
		}
	}

	private void onImageSelected(int resultCode, Intent data,
	                             WallPaperUpdater.Size size, int nextStepID)
	{
		if (resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			cropImage(uri, size, nextStepID);
		}
	}

	private void cropImage(Uri uri, WallPaperUpdater.Size size, int thisStepID)
	{
		File cacheFile = new File(MyApp.getContext().getCacheDir(), "crop");
		Uri outUri = Uri.fromFile(cacheFile);
		Crop crop = new Crop(uri);
		crop.output(outUri)
				.withAspect(size.weight, size.height)
				.withMaxSize(size.weight, size.height);

		if (mActivity != null) {
			crop.start(mActivity, thisStepID);
		} else {
			Activity pAct = mFragment.getActivity();
			crop.start(pAct, mFragment, thisStepID);
		}
	}


	private void selectPaper(int thisStepID)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");

		startActivityForResult(intent, thisStepID);
	}

	private void startActivityForResult(Intent intent, int requestCode)
	{
		if (mActivity != null) {
			mActivity.startActivityForResult(intent, requestCode);
		} else if (mFragment != null) {
			mFragment.startActivityForResult(intent, requestCode);
		} else {
			Log.e(TAG, "both mActivity and mFragment are null!");
		}
	}


	private static final int ID_SEL_WALLPAPER = 0x305;
	private static final int ID_CROP_WALLPAPER = 0x306;
	private static final int ID_PLACER_WALLPAPER = 0x307;

	private static final int ID_SEL_LOCKPAPER = 0x308;
	private static final int ID_CROP_LOCKPAPER = 0x309;
	private static final int ID_PLACER_LOCKPAPER = 0x30a;

	private static final String TAG = "WallPaperPicker";
	private Activity mActivity;
	private Fragment mFragment;
}
