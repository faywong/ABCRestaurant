package com.abcrestaurant.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.abcrestaurant.R;
import com.abcrestaurant.common.ABCConfig;

public class SettingActivity extends Activity {
	protected static final String TAG = "SettingActivity";

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_layout);
		Button saveSettingBtn = (Button)findViewById(R.id.savebtn);
		final EditText ipAddrEditText = (EditText)findViewById(R.id.ip_addr_setting_edittext);
	    final SharedPreferences ipAddrPrefs = getSharedPreferences(ABCConfig.IP_ADDR_SETTING_PREFS_KEY, MODE_PRIVATE);

		saveSettingBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor ipPrefsEditor = ipAddrPrefs.edit();
				String serverHostName = ipAddrEditText.getText().toString();
				Log.i(TAG,"The original serverHostName is " + serverHostName);
				ipPrefsEditor.putString(ABCConfig.IP_ADDR_SETTING_PREFS_KEY, serverHostName);
				ipPrefsEditor.commit();
				Toast.makeText(SettingActivity.this, R.string.setting_saved_prompt, Toast.LENGTH_SHORT);
				finish();
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
}
