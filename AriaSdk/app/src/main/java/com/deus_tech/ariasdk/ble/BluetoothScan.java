package com.deus_tech.ariasdk.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothScan {

	private static final String TAG = "TAG";

	private static final int LE_SCAN_PERIOD = 2000;
	private static final int LE_SCAN_DEVICE_TIMEOUT = 5000;

	private ArrayList<BluetoothDevice> mDeviceArray =  new ArrayList<>();

	private HashMap<String, Long> mLeDeviceLastResponseMap = new HashMap<>();
	private DiscoveryListener mDiscoveryListener;

	private BluetoothAdapter btAdapter;

	private BluetoothLeScanner mLEScanner;
	private ScanSettings mLESettings;
	private ArrayList<ScanFilter> mLEFilters;

	public interface DiscoveryListener {
		void onDeviceFound(BluetoothDevice device);
		void onDeviceLost(BluetoothDevice device);
		void onStarted();
		void onFinished(ArrayList<BluetoothDevice> deviceArray);
	}

	public BluetoothScan(BluetoothAdapter btAdapter) {
		this.btAdapter = btAdapter;
	}

	public void startLeScan(@NonNull DiscoveryListener listener) {
		Log.d("", "in btDiscoverDevices");

		mDeviceArray.clear();

		if (checkDeviceForBtSupport()) {
			//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				mLEScanner = btAdapter.getBluetoothLeScanner();
					mLESettings = new ScanSettings.Builder()
							.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
							//.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
							.build();
				mLEFilters = new ArrayList<>();

				mDiscoveryListener = null;
				stopLeScan();
				mDiscoveryListener = listener;

				mLEScanner.startScan(mLEFilters, mLESettings, mLeScanCallback);
				_discoveryLeHandler.removeCallbacks(_discoveryLeRunnable);
				_discoveryLeHandler.postDelayed(_discoveryLeRunnable, LE_SCAN_PERIOD);
				mDiscoveryListener.onStarted();
				Log.d(TAG, "LE start discovery ok");
			}
		} else {
			mDiscoveryListener.onFinished(null);
		}
	}

	public void stopLeScan() {
		if (mDiscoveryListener != null) {
			mDiscoveryListener.onFinished(null);
		}
		_discoveryLeHandler.removeCallbacks(_discoveryLeRunnable);
		mLeDeviceLastResponseMap.clear();
		if (checkDeviceForBtSupport()) {
			mLEScanner.stopScan(mLeScanCallback);
		}
	}

	private ScanCallback mLeScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			BluetoothDevice device = result.getDevice();
			String deviceName = device.getName();
			if (deviceName != null) {
				if (!mLeDeviceLastResponseMap.containsKey(deviceName)) {
					mDeviceArray.add(device);
					mDiscoveryListener.onDeviceFound(device);
					Log.d(TAG, "LE Device added to list (" + device.getName() + ", " + device.getAddress() + ")");
				}
				mLeDeviceLastResponseMap.put(deviceName, System.currentTimeMillis());
			}
			Log.i("callbackType", String.valueOf(callbackType));
			Log.i("result", result.toString());
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			for (ScanResult sr : results) {
				Log.i("ScanResult - Results", sr.toString());
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			Log.e("Scan Failed", "Error Code: " + errorCode);
			mDiscoveryListener.onFinished(null);
		}
	};


	private Handler _discoveryLeHandler = new Handler();
	private Runnable _discoveryLeRunnable = new Runnable() {
		public void run() {
			// Remove inactive devices from list
			for (int i = 0; i < mDeviceArray.size(); i++) {
				BluetoothDevice device = mDeviceArray.get(i);
				if (mLeDeviceLastResponseMap.get(device.getName()) + LE_SCAN_DEVICE_TIMEOUT < System.currentTimeMillis()) {
					mDeviceArray.remove(device);
					mLeDeviceLastResponseMap.remove(device.getName());
					mDiscoveryListener.onDeviceLost(device);
				}
			}
			_discoveryLeHandler.postDelayed(_discoveryLeRunnable, LE_SCAN_PERIOD);
		}
	};

	public boolean checkDeviceForBtSupport() {
		if (btAdapter != null) {
			Log.d(TAG, "Adapter OK");
			if (!btAdapter.isEnabled()) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<BluetoothDevice> getBluetoothDevices() {
		return mDeviceArray;
	}

}
