package com.zzz.androidibeacondemo.ui;

import java.util.HashSet;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.zzz.androidibeacondemo.R;
import com.zzz.androidibeacondemo.ibeacon.IBeacon;
import com.zzz.androidibeacondemo.service.IBeaconMonitorService;

/**
 * MainActivity
 * 
 * @author zzz
 *
 */
public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity";

	private final Messenger messenger = new Messenger(new IncomingHandler());
	public static final int MSG_STATE_SCANNING = 1;
	public static final int MSG_STATE_STOPPED = 2;
	public static final int MSG_SCAN_RESULT = 3;
	private Messenger service;

	private Button buttonStartScan;
	private TextView textviewState;
	private TextView textviewResult;

	private boolean bound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buttonStartScan = (Button) findViewById(R.id.button_start_scan);
		textviewResult = (TextView) findViewById(R.id.textview_result);
		textviewState = (TextView) findViewById(R.id.textview_state);
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
			MainActivity.this.service = new Messenger(service);
			bound = true;
			Log.v(TAG, "onServiceConnected");
			Message msg = Message.obtain(null,
					IBeaconMonitorService.MSG_CONNECTED, 0, 0);
			msg.replyTo = messenger;
			try {
				MainActivity.this.service.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
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
				Message msg = Message.obtain(null,
						IBeaconMonitorService.MSG_START_SCAN, 0, 0);
				try {
					service.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			break;
		default:
			Log.v(TAG, "unknown clicked");
		}
	}

	/**
	 * Handler of incoming messages from service
	 */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.v(TAG, "IncomingHandler.handleMessage");
			switch (msg.what) {
			case MSG_STATE_SCANNING:
				textviewState.setText(R.string.state_scanning);
				break;
			case MSG_STATE_STOPPED:
				textviewState.setText(R.string.state_stopped);
				break;
			case MSG_SCAN_RESULT:
				StringBuilder builder = new StringBuilder();
				HashSet<IBeacon> ibeaconSet = (HashSet<IBeacon>) msg.obj;
				for (IBeacon item : ibeaconSet) {
					builder.append(item.toString()).append("\n");
				}
				textviewResult.setText(builder.toString());
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
