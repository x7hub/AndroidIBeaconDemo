package com.zzz.androidibeacondemo.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.zzz.androidibeacondemo.R;
import com.zzz.androidibeacondemo.service.IBeaconMonitorService;

/**
 * MainActivity
 * 
 * @author zzz
 *
 */
public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity";

	private Button buttonStartScan;

	private boolean bound = false;
	private IBeaconMonitorService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buttonStartScan = (Button) findViewById(R.id.button_start_scan);
		buttonStartScan.setOnClickListener(this);

		Intent intent = new Intent(this.getApplicationContext(),
				IBeaconMonitorService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		if (bound) {
			unbindService(connection);
			bound = false;
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MainActivity.this.service = ((IBeaconMonitorService.IBeaconMonitorBinder) service)
					.getService();
			bound = true;
			Log.v(TAG, "onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			MainActivity.this.service = null;
			bound = false;
			Log.v(TAG, "onServiceDisconnected");
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_start_scan:
			Log.v(TAG, "button_start_scan clicked");
			if (bound) {
				service.startScan();
			}
			break;
		default:
			Log.v(TAG, "unknown clicked");
		}
	}
}
