AndroidIBeaconDemo
==================

Implement Apple IBeacon on Android

### 背景资料

[这里](http://stackoverflow.com/questions/18906988/what-is-the-ibeacon-bluetooth-profile)详细介绍了iBeacon协议中的关键字段，此Demo根据协议中的字段，将蓝牙扫描到的记录字段做解析，判断记录是否是iBeacon包，并获得iBeacon中的信息字段，包括UUID，major，minor，txpower。

>02 # Number of bytes that follow in first AD structure

>01 # Flags AD type

>1A # Flags value 0x1A = 000011010

>   bit 0 (OFF) LE Limited Discoverable Mode

>   bit 1 (ON) LE General Discoverable Mode

>   bit 2 (OFF) BR/EDR Not Supported

>   bit 3 (ON) Simultaneous LE and BR/EDR to Same Device Capable (controller)

>   bit 4 (ON) Simultaneous LE and BR/EDR to Same Device Capable (Host)

>1A # Number of bytes that follow in second (and last) AD structure

>FF # Manufacturer specific data AD type

>4C 00 # Company identifier code (0x004C == Apple)

>02 # Byte 0 of iBeacon advertisement indicator

>15 # Byte 1 of iBeacon advertisement indicator

>e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon proximity uuid

>00 00 # major

>00 00 # minor

>c5 # The 2's complement of the calibrated Tx Power

### 关键实现步骤

通过`BluetoothAdapter`启动低功耗蓝牙的扫描，指定LeScanCallBack作为参数处理接收到的记录，这部分操作在`IBeaconMonitorService.java`中。

```java
// start le scan
boolean isScanStarted = getBluetoothAdapter().startLeScan(leScanCallBack);
```

```java
private BluetoothAdapter.LeScanCallback leScanCallBack = new BluetoothAdapter.LeScanCallback() {
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
    // 这里的scanRecord就是要处理的记录
    }
}
```

处理接收到的记录，这是一个byte数组，根据协议规定判断是否是iBeacon包，再取出相应位置的字段获取信息，这部分代码在`IBeacon.java`中，

```java

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
```

### 另外

GitHub上[有一个工程](https://github.com/RadiusNetworks/android-ibeacon-service)是一个公司专门在Android上做的iBeacon的移植，但是由于不明原因被删除的公开的代码，不过在[其历史提交记录](https://github.com/RadiusNetworks/android-ibeacon-service/tree/800a1d1b24e1d5f13f4589412ce5c6bf3f7bc3f1)中还是可以看到删除前的代码，至于删除原因该公司没有说明，给出的咨询邮箱没有回复。