package com.zzz.androidibeacondemo.ibeacon;

import android.util.Log;

/**
 * IBeacon
 * 
 * @author zzz
 *
 */
public class IBeacon {
	private static final String TAG = "IBeacon";

	public String uuid;
	public int major;
	public int minor;
	public int txpower;

	public IBeacon() {
		uuid = "";
		major = 0;
		minor = 0;
		txpower = 0;
	}

	public IBeacon(String uuid, int major, int minor, int txpower) {
		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
		this.txpower = txpower;
	}

	public IBeacon readFromArray(byte[] src) {
		// see
		// http://stackoverflow.com/questions/18906988/what-is-the-ibeacon-bluetooth-profile
		Log.v(TAG, this.bytesToHexString(src));

		if (((int) (src[7] & 0xff) | ((int) (src[8] & 0xff)) << 8) != 0x1502) {
			Log.d(TAG, "this is not ibeacon !");
			return this;
		}

		Log.d(TAG, "find ibeacon !");

		// read uuid
		byte[] arrayUuid = new byte[16];
		System.arraycopy(src, 9, arrayUuid, 0, 16);
		Log.i(TAG, "uuid - " + this.bytesToHexString(arrayUuid));
		this.uuid = this.bytesToHexString(arrayUuid);

		// read major
		this.major = (int) (src[25] & 0xff) | ((int) (src[26] & 0xff)) << 8;
		Log.i(TAG, "major - " + Integer.toHexString(major));

		// read minor
		this.minor = (int) (src[27] & 0xff) | ((int) (src[28] & 0xff)) << 8;
		Log.i(TAG, "minor - " + Integer.toHexString(minor));

		// read txpower
		this.txpower = (int) src[29];
		Log.i(TAG, "txpower - " + txpower);

		return this;
	}

	private String bytesToHexString(byte[] src) {
		StringBuilder builder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				builder.append(0);
			}
			builder.append(hv);
		}
		return builder.toString();
	}
}
