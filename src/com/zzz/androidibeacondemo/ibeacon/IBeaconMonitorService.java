package com.zzz.androidibeacondemo.ibeacon;

import java.util.HashSet;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.zzz.androidibeacondemo.ui.MainActivity;

/**
 * IBeaconMonitorService
 * 
 * @author zzz
 *
 */
@TargetApi(18)
public class IBeaconMonitorService extends Service {
	private static final String TAG = "IBeaconMonitorService";

	private static final long SCAN_TIME = 10000;

	private final Messenger messenger = new Messenger(new IncomingHandler());
	public static final int MSG_CONNECTED = 1;
	public static final int MSG_START_SCAN = 2;
	private Messenger client;

	private BluetoothAdapter bluetoothAdapter;

	private Handler handler;

	private HashSet<IBeacon> ibeaconSet;

	public class IBeaconMonitorBinder extends Binder {
		public IBeaconMonitorService getService() {
			return IBeaconMonitorService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		HandlerThread thread = new HandlerThread("ServiceStartArguments");
		thread.start();
		handler = new Handler(thread.getLooper());
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.v(TAG, "onBind");
		return messenger.getBinder();
	}

	private BluetoothAdapter getBluetoothAdapter() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			Log.w(TAG, "getBluetoothAdapter need API 18; return");
			return null;
		}
		if (bluetoothAdapter == null) {
			final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			bluetoothAdapter = bluetoothManager.getAdapter();
		}
		return bluetoothAdapter;
	}

	private BluetoothAdapter.LeScanCallback leScanCallBack = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Log.v(TAG, "onLeScan");
			Log.i(TAG, "device - " + device.getAddress());
			Log.i(TAG, "rssi - " + rssi);
			IBeacon ibeacon = new IBeacon();
			ibeacon.readFromArray(scanRecord);
			if (!ibeacon.isEmpty()) {
				// add to hash set
				ibeaconSet.add(ibeacon);
				// update ui
				Message msg = Message.obtain(null,
						MainActivity.MSG_SCAN_RESULT, 0, 0);
				msg.obj = ibeaconSet;
				try {
					client.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
		}

	};

	private Runnable startScanRunnable = new Runnable() {

		@Override
		@TargetApi(18)
		public void run() {
			Log.d(TAG, "startScanRunnable");
			ibeaconSet = new HashSet<IBeacon>();
			// start le scan
			boolean isScanStarted = getBluetoothAdapter().startLeScan(
					leScanCallBack);
			Log.i(TAG, "isScanStarted - " + isScanStarted);
			if (isScanStarted) {
				handler.postDelayed(stopScanRunnable, SCAN_TIME);

				// update ui
				Message msgSetState = Message.obtain(null,
						MainActivity.MSG_STATE_SCANNING, 0, 0);
				try {
					client.send(msgSetState);
				} catch (RemoteException e) {
					e.printStackTrace();
				}

				// update ui
				Message msgClearResult = Message.obtain(null,
						MainActivity.MSG_SCAN_RESULT, 0, 0);
				msgClearResult.obj = ibeaconSet;
				try {
					client.send(msgClearResult);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

	};

	private Runnable stopScanRunnable = new Runnable() {

		@Override
		public void run() {
			Log.d(TAG, "stopScanRunnable");
			// stop le scan
			getBluetoothAdapter().stopLeScan(leScanCallBack);

			// update ui
			Message msg = Message.obtain(null, MainActivity.MSG_STATE_STOPPED,
					0, 0);
			try {
				client.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	};

	/**
	 * methods for client
	 */
	private void startScan() {
		Log.v(TAG, "startScan");

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			Log.w(TAG, "startScan need API 18; return");
			return;
		}

		try {
			if (getBluetoothAdapter() != null
					&& getBluetoothAdapter().isEnabled()) {
				handler.post(startScanRunnable);
			} else {
				Log.w(TAG, "Bluetooth is not enabled");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Handler of incoming messages from clients
	 */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.v(TAG, "IncomingHandler.handleMessage");
			switch (msg.what) {
			case MSG_CONNECTED:
				client = msg.replyTo;
				break;
			case MSG_START_SCAN:
				startScan();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
}
