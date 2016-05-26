package com.deus_tech.ariasdk.ble;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import com.deus_tech.ariasdk.ariaBleService.ArsGattListener;
import com.deus_tech.ariasdk.ariaBleService.AriaBleService;
import com.deus_tech.ariasdk.calibrationBleService.CasGattListener;
import com.deus_tech.ariasdk.calibrationBleService.CalibrationBleService;


import java.util.List;

public class BluetoothGattCallback extends android.bluetooth.BluetoothGattCallback{


    private ConnectionGattListener connectionListener;
    private CasGattListener calibrationListener;
    private ArsGattListener arsListener;


    public void setConnectionListener(ConnectionGattListener _connectionListener){

        connectionListener = _connectionListener;

    }//setConnectionListener


    public void setCalibrationListener(CasGattListener _calibrationListener){

        calibrationListener = _calibrationListener;

    }//setCalibrationListener


    public void setArsListener(ArsGattListener _arsListener){

        arsListener = _arsListener;

    }//setArsListener


    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){

        if(newState == BluetoothProfile.STATE_CONNECTED){

            gatt.discoverServices();

        }else if(newState == BluetoothProfile.STATE_DISCONNECTED){

            gatt.close();

            if(connectionListener != null){
                connectionListener.onDeviceDisconnected();
            }

        }

    }//onConnectionStateChange


    public void	onServicesDiscovered(BluetoothGatt gatt, int status){

        if(status == BluetoothGatt.GATT_SUCCESS){

            List<BluetoothGattService> services = gatt.getServices();
//            List<BluetoothGattService> services = new ArrayList<>();
//	        BluetoothGattService service = gatt.getService(AriaBleService.ARIA_SERVICE_UUID);
//	        if (service != null) {
//		        services.add(service);
//	        }
//	        service = gatt.getService(CalibrationBleService.CALIBRATION_SERVICE_UUID);
//	        if (service != null) {
//		        services.add(service);
//	        }

            if(connectionListener != null){
                connectionListener.onDeviceConnected(services);
            }

        }

    }//onServicesDiscovered


    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){

        if(status == BluetoothGatt.GATT_SUCCESS){

            if(characteristic.getUuid().equals(CalibrationBleService.CALIBRATION_ATTRIBUTE_UUID) && calibrationListener != null){

                calibrationListener.onCalibrationAttributeRead(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.CALIBRATION_DATETIME_UUID) && calibrationListener != null){

                calibrationListener.onCalibrationDatetimeRead(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.CALIBRATION_MODE_UUID) && calibrationListener != null){

                calibrationListener.onCalibrationModeRead(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.SETTINGS_COMMAND_UUID) && calibrationListener != null){

                calibrationListener.onSettingsCommandRead(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.SETTINGS_DATA_UUID) && calibrationListener != null){

                calibrationListener.onSettingsDataRead(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.GESTURE_STATUS_UUID) && calibrationListener != null){

                calibrationListener.onGestureStatusRead(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

            }else if(characteristic.getUuid().equals(AriaBleService.ARIA_BATTERY_UUID) && arsListener != null){

                arsListener.onBatteryRead(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

            }

        }

    }//onCharacteristicRead


    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){

        if(status == BluetoothGatt.GATT_SUCCESS){

           if(characteristic.getUuid().equals(CalibrationBleService.CALIBRATION_DATETIME_UUID) && calibrationListener != null){

                calibrationListener.onCalibrationDatetimeWritten(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.CALIBRATION_MODE_UUID) && calibrationListener != null){

                calibrationListener.onCalibrationModeWritten(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.SETTINGS_COMMAND_UUID) && calibrationListener != null){

                calibrationListener.onSettingsCommandWritten(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.SETTINGS_DATA_UUID) && calibrationListener != null){

                calibrationListener.onSettingsDataWritten(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0));

            }else if(characteristic.getUuid().equals(CalibrationBleService.GESTURE_STATUS_UUID) && calibrationListener != null){

                calibrationListener.onGestureStatusWritten(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0));

            }

        }

    }//onCharacteristicWrite


    public void	onReliableWriteCompleted(BluetoothGatt gatt, int status){}//onReliableWriteCompleted


    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){

        if(characteristic.getUuid().equals(CalibrationBleService.CALIBRATION_ATTRIBUTE_UUID) && calibrationListener != null){

            calibrationListener.onCalibrationAttributeChanged(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8,0));

        }else if(characteristic.getUuid().equals(CalibrationBleService.GESTURE_STATUS_UUID) && calibrationListener != null){

            calibrationListener.onGestureStatusNotifyChanged(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

        }else if(characteristic.getUuid().equals(AriaBleService.ARIA_GESTURE_UUID) && arsListener != null){

            arsListener.onGestureChanged(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

        }else if(characteristic.getUuid().equals(AriaBleService.ARIA_BATTERY_UUID) && arsListener != null){

            arsListener.onBatteryChanged(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));

        }

    }//onCharacteristicChanged


    public void	onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){}//onDescriptorRead


    public void	onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){

        if(status == BluetoothGatt.GATT_SUCCESS){

            if(descriptor.getCharacteristic().getUuid().equals(CalibrationBleService.CALIBRATION_ATTRIBUTE_UUID) && calibrationListener != null){

                if(descriptor.getValue().equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){

                    calibrationListener.onCalibrationAttributeNotifyEnabled();

                }else if(descriptor.getValue().equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){

                    calibrationListener.onCalibrationAttributeNotifyDisabled();

                }

            }else if(descriptor.getCharacteristic().getUuid().equals(CalibrationBleService.GESTURE_STATUS_UUID) && calibrationListener != null){

                if(descriptor.getValue().equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){

                    calibrationListener.onGestureStatusNotifyEnabled();

                }else if(descriptor.getValue().equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){

                    calibrationListener.onGestureStatusNotifyDisabled();

                }

            }else if(descriptor.getCharacteristic().getUuid().equals(AriaBleService.ARIA_GESTURE_UUID) && arsListener != null){

                if(descriptor.getUuid().equals(AriaBleService.CCCD_UUID)){

                    if(descriptor.getValue().equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){

                        arsListener.onGestureNotifyEnabled();

                    }else if(descriptor.getValue().equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){

                        arsListener.onGestureNotifyDisabled();

                    }

                }

            }else if(descriptor.getCharacteristic().getUuid().equals(AriaBleService.ARIA_BATTERY_UUID) && arsListener != null){

                if(descriptor.getUuid().equals(AriaBleService.CCCD_UUID)){

                    if(descriptor.getValue().equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){

                        arsListener.onBatteryNotifyEnabled();

                    }else if(descriptor.getValue().equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){

                        arsListener.onBatteryNotifyDisabled();

                    }

                }

            }

        }

    }//onDescriptorWrite

    //mtu = maximum transmission unit
    public void	onMtuChanged(BluetoothGatt gatt, int mtu, int status){}//onMtuChanged


    public void	onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status){}//onReadRemoteRssi


}//WeesBluetoothGattCallback