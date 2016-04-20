package com.deus_tech.ariasdk.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class BluetoothScan {

	private static final String TAG = "TAG";

	private static final int LE_SCAN_PERIOD = 2000;
	private static final int LE_SCAN_DEVICE_TIMEOUT = 4000;

	private ArrayList<BluetoothDevice> mDeviceArray =  new ArrayList<>();

	private HashMap<String, Long> mLeDeviceLastResponseMap = new HashMap<>();
	private DiscoveryListener mDiscoveryListener;

	private BluetoothAdapter btAdapter;

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

		mDiscoveryListener = null;
		stopLeScan();
		mDiscoveryListener = listener;

		mDeviceArray.clear();

		if (checkDeviceForBtSupport()) {
			if (btAdapter.startLeScan(mLeScanCallback)) {
				_discoveryLeHandler.removeCallbacks(_discoveryLeRunnable);
				_discoveryLeHandler.postDelayed(_discoveryLeRunnable, LE_SCAN_PERIOD);
				mDiscoveryListener.onStarted();
				Log.d(TAG, "LE start discovery ok");
			} else {
				mDiscoveryListener.onFinished(null);
				Log.d(TAG, "LE Discovery failed.");
				//Discovery failed.
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
			btAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			String deviceName = device.getName();
			if (deviceName != null) {
				if (!mLeDeviceLastResponseMap.containsKey(deviceName)) {
					mDeviceArray.add(device);
					mDiscoveryListener.onDeviceFound(device);
					Log.d(TAG, "LE Device added to list (" + device.getName() + ", " + device.getAddress() + ")");
				}
				mLeDeviceLastResponseMap.put(deviceName, System.currentTimeMillis());
			}
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
