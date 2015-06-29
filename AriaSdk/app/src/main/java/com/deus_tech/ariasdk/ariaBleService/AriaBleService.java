package com.deus_tech.ariasdk.ariaBleService;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;

public class AriaBleService implements ArsGattListener{

    //UUID-s
    public final static UUID ARIA_SERVICE_UUID = UUID.fromString("e95d0000-b0de-1051-43b0-c7ab0ceffe1a");
    public final static UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID ARIA_GESTURE_UUID = UUID.fromString("e95d0001-b0de-1051-43b0-c7ab0ceffe1a");
    public final static UUID ARIA_BATTERY_UUID = UUID.fromString("e95d0002-b0de-1051-43b0-c7ab0ceffe1a");

    //gestures
    public final static int GESTURE_HOME = 1; //right - 4F
    public final static int GESTURE_ENTER = 2; //enter - 28
    public final static int GESTURE_BACK = 3; //down - 51
    public final static int GESTURE_UP = 4; //up - 52
    public final static int GESTURE_DOWN = 5; //left - 50

    private Context context;
    //bluetooth
    private BluetoothGatt btGatt;
    private BluetoothGattService btGattService;
    //characteristics
    private BluetoothGattCharacteristic ariaGestureChar;
    private BluetoothGattCharacteristic ariaBatteryChar;
    //listeners
    private ArrayList<ArsInitListener> initListeners;
    private ArrayList<ArsListener> arsListeners;


    public AriaBleService(Context _context, BluetoothGatt _btGatt, BluetoothGattService _btGattService){

        context = _context;

        initListeners = new ArrayList<ArsInitListener>();
        arsListeners = new ArrayList<ArsListener>();

        btGatt = _btGatt;
        btGattService = _btGattService;

    }//ArsService


    public void addInitListener(ArsInitListener _listener){

        initListeners.add(_listener);

    }//addInitListener


    public void removeInitListener(ArsInitListener _listener){

        initListeners.remove(_listener);

    }//removeInitListener


    public void addListener(ArsListener _listener){

        arsListeners.add(_listener);

    }//addArsListener


    public void removeListener(ArsListener _listener){

        arsListeners.remove(_listener);

    }//removeArsListener


    public void init(){

        ariaGestureChar = btGattService.getCharacteristic(AriaBleService.ARIA_GESTURE_UUID);
        ariaBatteryChar = btGattService.getCharacteristic(AriaBleService.ARIA_BATTERY_UUID);

        if(ariaGestureChar != null){
            enableGestureNotify(true);
        }

    }//init


    public void readBattery(){

        btGatt.readCharacteristic(ariaBatteryChar);

    }//readBattery


    //private

    private void enableGestureNotify(boolean _isEnabled){

        btGatt.setCharacteristicNotification(ariaGestureChar, true);

        BluetoothGattDescriptor cccd = ariaGestureChar.getDescriptor(AriaBleService.CCCD_UUID);

        if(_isEnabled){
            cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else{
            cccd.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        btGatt.writeDescriptor(cccd);

    }//enableGestureNotify


    private void enableBatteryNotify(boolean _isEnabled){

        btGatt.setCharacteristicNotification(ariaBatteryChar, true);

        BluetoothGattDescriptor cccd = ariaBatteryChar.getDescriptor(AriaBleService.CCCD_UUID);

        if(_isEnabled){
            cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else{
            cccd.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        btGatt.writeDescriptor(cccd);

    }//enableBatteryNotify


    //ArsGattListener - gesture

    public void onGestureNotifyEnabled(){

        if(ariaBatteryChar != null){
            enableBatteryNotify(true);
        }

    }//onGestureNotifyEnabled


    public void onGestureNotifyDisabled(){}//onGestureNotifyDisabled


    public void onGestureChanged(int _value){

        if(_value == 40) _value = AriaBleService.GESTURE_ENTER;
        else if(_value == 82) _value = AriaBleService.GESTURE_UP;
        else if(_value == 81) _value = AriaBleService.GESTURE_BACK;
        else if(_value == 79) _value = AriaBleService.GESTURE_HOME;
        else _value = AriaBleService.GESTURE_DOWN;

        for(int i=0 ; i<arsListeners.size() ; i++){
            arsListeners.get(i).onGesturePerformed(_value);
        }

    }//onGestureChanged


    //ArsGattListener - battery

    public void onBatteryRead(int _value){

        for(int i=0 ; i<arsListeners.size() ; i++){
            arsListeners.get(i).onBatteryValueUpdated(_value);
        }

    }//onBatteryRead


    public void onBatteryNotifyEnabled(){

        for(int i=0 ; i<initListeners.size() ; i++){
            initListeners.get(i).onArsInit();
        }

    }//onBatteryNotifyEnabled


    public void onBatteryNotifyDisabled(){}//onBatteryNotifyDisabled


    public void onBatteryChanged(int _value){

        for(int i=0 ; i<arsListeners.size() ; i++){
            arsListeners.get(i).onBatteryValueUpdated(_value);
        }

    }//onBatteryChanged


}//AriaBleService
