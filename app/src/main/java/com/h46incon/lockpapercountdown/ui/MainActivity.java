package com.h46incon.lockpapercountdown.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.h46incon.lockpapercountdown.tools.WallPaperPicker;
import com.h46incon.lockpapercountdown.tools.WallPaperUpdater;
import com.h46incon.lockpapercountdown.util.MyApp;

/**
 * Created by Administrator on 2014/8/25.
 */
public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		picker = new WallPaperPicker(this);

		Intent intent = getIntent();
		String action = intent.getAction();

		if (Intent.ACTION_MAIN.equals(action)) {
			// Display the fragment as the main content.
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingFragment())
					.commit();
		} else if (Intent.ACTION_SEND.equals(action)) {
			onFileSend(intent);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		picker.onActivityResult(requestCode, resultCode, data);
	}

	private void onFileSend(Intent intent)
	{
		// some image is sent to this
		String type = intent.getType();
		if (!type.startsWith("image/")) {
			MyApp.showSimpleToast("Unknown file types");
			return;
		}

		final Uri data = intent.getParcelableExtra(Intent.EXTRA_STREAM);

		String[] items;
		if (WallPaperUpdater.couldSetLockPaper()) {
			items = new String[]{"主屏幕壁纸", "锁屏壁纸"};
		} else {
			items = new String[]{"主屏幕壁纸"};
		}

		DialogInterface.OnClickListener onItemSelected = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which)
			{
				WallPaperPicker.PaperType type;

				switch (which) {
					case 0:
						type = WallPaperPicker.PaperType.WALL_PAPER;
						break;

					case 1:
						type = WallPaperPicker.PaperType.LOCK_PAPER;
						break;

					default:
						MyApp.showSimpleToast("Unknown choice");
						dialog.dismiss();
						return;

				}

				picker.startFromCropper(type, data);
				dialog.dismiss();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("选择壁纸类型")
				.setSingleChoiceItems(
						items,
						-1,
						onItemSelected)
				.setNegativeButton("取消", null);

		AlertDialog dialog = builder.create();
		dialog.show();


	}

	private WallPaperPicker picker;
}
