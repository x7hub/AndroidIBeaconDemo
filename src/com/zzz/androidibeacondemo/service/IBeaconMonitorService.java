package com.zzz.androidibeacondemo.service;

import java.util.List;

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
import android.util.Log;

import com.zzz.androidibeacondemo.ibeacon.IBeacon;

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

	private BluetoothAdapter bluetoothAdapter;

	private IBeaconMonitorBinder binder = new IBeaconMonitorBinder();
	private Handler handler;

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
		return binder;
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
			Log.i(TAG, scanRecord.toString());
		}

	};

	private Runnable startScanRunnable = new Runnable() {

		@Override
		@TargetApi(18)
		public void run() {
			Log.d(TAG, "startScanRunnable");
			// start le scan
			boolean isScanStarted = getBluetoothAdapter().startLeScan(
					leScanCallBack);
			Log.i(TAG, "isScanStarted - " + isScanStarted);
			if (isScanStarted) {
				handler.postDelayed(stopScanRunnable, SCAN_TIME);
			}
		}

	};

	private Runnable stopScanRunnable = new Runnable() {

		@Override
		public void run() {
			Log.d(TAG, "stopScanRunnable");
			// stop le scan
			getBluetoothAdapter().stopLeScan(leScanCallBack);
		}

	};

	/**
	 * methods for client
	 */
	public List<IBeacon> startScan() {
		Log.v(TAG, "startScan");

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			Log.w(TAG, "startScan need API 18; return");
			return null;
		}

		try {
			if (getBluetoothAdapter() != null
					&& getBluetoothAdapter().isEnabled()) {
				handler.post(startScanRunnable);
			} else {
				Log.w(TAG, "Bluetooth is not enabled");
			}
		} catch (Exception e) {
			Log.w(TAG, e.toString());
		}

		return null;
	}
}
