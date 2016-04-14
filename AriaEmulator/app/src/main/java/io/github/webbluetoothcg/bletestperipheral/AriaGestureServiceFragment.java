package io.github.webbluetoothcg.bletestperipheral;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AriaGestureServiceFragment extends ServiceFragment implements View.OnClickListener {

	public static final UUID ARIA_SERVICE_UUID = UUID.fromString("e95d0000-b0de-1051-43b0-c7ab0ceffe1a");
    public static final UUID ARIA_GESTURE_UUID = UUID.fromString("e95d0001-b0de-1051-43b0-c7ab0ceffe1a");
	public static final UUID ARIA_BATTERY_UUID = UUID.fromString("e95d0002-b0de-1051-43b0-c7ab0ceffe1a");
    public static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int GESTURE_BACK = 81;
    public static final int GESTURE_DOWN = 80;
    public static final int GESTURE_ENTER = 40;
    public static final int GESTURE_UP = 82;

	private ServiceFragmentDelegate mDelegate;

    private Button mUpButton;
    private Button mDownButton;
    private Button mEnterButton;
    private Button mBackButton;

    private BluetoothGattService mAriaGestureService;
    private BluetoothGattCharacteristic mGectureCharacteristic;
	private BluetoothGattCharacteristic mBatteryCharacteristic;

	private BluetoothGattService mCalibrationService;

	public AriaGestureServiceFragment() {
		initAriaService();
		initCalibrationService();
	}

	private void initAriaService() {
		mGectureCharacteristic =
				new BluetoothGattCharacteristic(ARIA_GESTURE_UUID,
						BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
						BluetoothGattCharacteristic.PERMISSION_READ);

		mGectureCharacteristic.addDescriptor(
				new BluetoothGattDescriptor(CCCD_UUID,
						(BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE)));

		mBatteryCharacteristic =
				new BluetoothGattCharacteristic(ARIA_BATTERY_UUID,
						BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
						BluetoothGattCharacteristic.PERMISSION_READ);

		mBatteryCharacteristic.addDescriptor(
				new BluetoothGattDescriptor(CCCD_UUID,
						(BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE)));

		mAriaGestureService = new BluetoothGattService(ARIA_SERVICE_UUID,
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		mAriaGestureService.addCharacteristic(mGectureCharacteristic);
		mAriaGestureService.addCharacteristic(mBatteryCharacteristic);
	}

	private void initCalibrationService() {
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aria, container, false);

        mUpButton = (Button) view.findViewById(R.id.button_up);
        mDownButton = (Button) view.findViewById(R.id.button_down);
        mEnterButton = (Button) view.findViewById(R.id.button_enter);
        mBackButton = (Button) view.findViewById(R.id.button_back);

        mUpButton.setOnClickListener(this);
        mDownButton.setOnClickListener(this);
        mEnterButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDelegate = (ServiceFragmentDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ServiceFragmentDelegate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

	@Override
	public List<BluetoothGattService> getBluetoothGattServices() {
		List<BluetoothGattService> services = new ArrayList<>();
		services.add(mAriaGestureService);
		return services;
	}

    @Override
    public ParcelUuid getServiceUUID() {
        return new ParcelUuid(ARIA_SERVICE_UUID);
    }

    @Override
    public void onClick(View v) {
        if (v == mUpButton){
            mGectureCharacteristic.setValue(GESTURE_UP, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        } else if (v == mDownButton){
            mGectureCharacteristic.setValue(GESTURE_DOWN, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        } else if (v == mEnterButton) {
            mGectureCharacteristic.setValue(GESTURE_ENTER, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        } else if (v == mBackButton) {
            mGectureCharacteristic.setValue(GESTURE_BACK, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        }

        mDelegate.sendNotificationToDevices(mGectureCharacteristic);
    }

}