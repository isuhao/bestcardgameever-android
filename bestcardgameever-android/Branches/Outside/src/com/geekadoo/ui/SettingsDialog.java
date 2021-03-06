package com.geekadoo.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.geekadoo.R;
import com.geekadoo.R.id;

public class SettingsDialog extends Dialog implements
		android.view.View.OnClickListener {
	private static final String PREFS_NAME = "YANIV_PREFS";
	private static final String SILENT_MODE_PROPERTY = "silentMode";

	Button okButton;
//	Button cancelButton;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		okButton = (Button) findViewById(id.settingsDialogOkButton);
		okButton.setOnClickListener(this);
//		cancelButton = (Button) findViewById(id.settingsDialogCancelButton);
//		cancelButton.setOnClickListener(this);
		final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.soundToggle);
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
	       boolean silent = settings.getBoolean(SILENT_MODE_PROPERTY, false);

		togglebutton.setChecked(silent);
		togglebutton.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// We need an Editor object to make preference changes.
					      // All objects are from android.context.Context
					      SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
					      SharedPreferences.Editor editor = settings.edit();
					      editor.putBoolean("silentMode", isChecked);
					      // Commit the edits!
					      editor.commit();
					}
				});
	}

	public SettingsDialog(Context context) {
		super(context);
		this.context = context;

		this.setTitle(R.string.settingsDialogHeading);
		setContentView(R.layout.settings_view);

		// Have the system blur any windows behind this one.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.settingsDialogOkButton:
//			saveSettings();
			dismiss();
			break;
//		case R.id.settingsDialogCancelButton:
//			dismiss();
//			break;
		default:
			break;
		}
	}

//	private void saveSettings() {
//		Log.e("Settings", "save settings now");
//	}
}