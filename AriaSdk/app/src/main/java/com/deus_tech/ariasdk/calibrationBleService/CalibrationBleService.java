package com.deus_tech.ariasdk.calibrationBleService;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.deus_tech.ariasdk.R;

import java.util.ArrayList;
import java.util.UUID;

public class CalibrationBleService implements CasGattListener{

    //UUID-s
    public final static UUID CALIBRATION_SERVICE_UUID = UUID.fromString("caa50000-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID CALIBRATION_QUALITY_UUID = UUID.fromString("caa50001-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID CALIBRATION_DATETIME_UUID = UUID.fromString("caa50002-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID CALIBRATION_MODE_UUID = UUID.fromString("caa50003-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID GESTURE_INDEX_UUID = UUID.fromString("caa50004-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID GESTURE_ITERATION_UUID = UUID.fromString("caa50005-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID GESTURE_STATUS_UUID = UUID.fromString("caa50006-2244-a09d-e968-5f43e74d0c5c");


    //calibration quality values
    /*
    public final static int CALIBRATION_QUALITY_BAD = 1;
    public final static int CALIBRATION_QUALITY_MEDIUM = 2;
    public final static int CALIBRATION_QUALITY_GOOD = 3;
    */

    //calibration mode values
    public final static int CALIBRATION_MODE_NONE = 0;
    public final static int CALIBRATION_MODE_HARD = 1;
    //public final static int CALIBRATION_MODE_SOFT = 2;

    //gesture status values
    public final static int GESTURE_STATUS_NONE = 0;
    public final static int GESTURE_STATUS_STARTED = 1;
    public final static int GESTURE_STATUS_RECORDING = 2;
    public final static int GESTURE_STATUS_OK = 3;
    public final static int GESTURE_STATUS_ERROR1 = 4;
    //public final static int GESTURE_STATUS_ERROR2 = 5;
    //public final static int GESTURE_STATUS_ERROR3 = 6;


    private Context context;
    //bluetooth
    private BluetoothGatt btGatt;
    private BluetoothGattService btGattService;
    //characteristics
    private BluetoothGattCharacteristic calibrationQualityChar;
    private BluetoothGattCharacteristic calibrationDatetimeChar;
    private BluetoothGattCharacteristic calibrationModeChar;
    private BluetoothGattCharacteristic gestureIndexChar;
    private BluetoothGattCharacteristic gestureIterationChar;
    private BluetoothGattCharacteristic gestureStatusChar;
    //listeners
    private ArrayList<CasInitListener> initListeners;
    private ArrayList<CasListener> casListeners;
    //calibration
    private int numGestures;
    private int numIterations;
    private int calibrationStatus;
    private int gestureStatus;
    private int currentGestureIndex;
    private int currentGestureIteration;


    public CalibrationBleService(Context _context, BluetoothGatt _btGatt, BluetoothGattService _btGattService){

        context = _context;

        numGestures = context.getResources().getInteger(R.integer.calibration_gestures);
        numIterations = context.getResources().getInteger(R.integer.calibration_iterations);

        initListeners = new ArrayList<CasInitListener>();
        casListeners = new ArrayList<CasListener>();

        btGatt = _btGatt;
        btGattService = _btGattService;

    }//CalibrationService


    public void addInitListener(CasInitListener _listener){

        initListeners.add(_listener);

    }//addInitListener


    public void removeInitListener(CasInitListener _listener){

        initListeners.remove(_listener);

    }//removeInitListener


    public void addCasListener(CasListener _listener){

        casListeners.add(_listener);

    }//addCasListener


    public void removeCasListener(CasListener _listener){

        casListeners.remove(_listener);

    }//removeCasListener


    public void init(){

        calibrationStatus = CalibrationBleService.CALIBRATION_MODE_NONE;
        gestureStatus = CalibrationBleService.GESTURE_STATUS_NONE;

        calibrationQualityChar = btGattService.getCharacteristic(CalibrationBleService.CALIBRATION_QUALITY_UUID);
        calibrationDatetimeChar = btGattService.getCharacteristic(CalibrationBleService.CALIBRATION_DATETIME_UUID);
        calibrationModeChar = btGattService.getCharacteristic(CalibrationBleService.CALIBRATION_MODE_UUID);
        gestureIndexChar = btGattService.getCharacteristic(CalibrationBleService.GESTURE_INDEX_UUID);
        gestureIterationChar = btGattService.getCharacteristic(CalibrationBleService.GESTURE_ITERATION_UUID);
        gestureStatusChar = btGattService.getCharacteristic(CalibrationBleService.GESTURE_STATUS_UUID);

        enableGestureStatusNotify(true);

    }//init


    public int getGesturesNumber(){

        return numGestures;

    }//getGesturesNumber


    public int getIterationsNumber(){

        return numIterations;

    }//getIterationsNumber


    public void startCalibration(){

        calibrationStatus = CalibrationBleService.CALIBRATION_MODE_NONE;
        gestureStatus = CalibrationBleService.GESTURE_STATUS_NONE;

        currentGestureIndex = 1;
        currentGestureIteration = 0;

        writeCalibrationMode(CalibrationBleService.CALIBRATION_MODE_HARD);

    }//startCalibration


    public void nextCalibrationStep(){

        currentGestureIteration++;

        if(currentGestureIteration > numIterations){

            currentGestureIndex++;
            currentGestureIteration = 1;

        }

        if(currentGestureIndex > numGestures){

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationFinished();
            }

        }else{

            writeGestureIndex(currentGestureIndex);

        }

    }//nextCalibrationStep


    public void repeatCalibrationStep(){

        writeGestureIndex(this.currentGestureIndex);

    }//repeatCalibrationStep


    public void stopCalibration(){

        writeCalibrationMode(CalibrationBleService.CALIBRATION_MODE_NONE);

    }//stopCalibration


    public int getCalibrationStatus(){

        return calibrationStatus;

    }//getCalibrationStatus


    public int getGestureStatus(){

      return gestureStatus;

    }//getGestureStatus


    public int getGestureIndex(){

        return currentGestureIndex;

    }//getGestureIndex


    public int getGestureIteration(){

        return currentGestureIteration;

    }//getGestureIteration


    //read

    private void readCalibrationQuality(){

        btGatt.readCharacteristic(calibrationQualityChar);

    }//readCalibrationQuality


    private void readCalibrationDatetime(){

        btGatt.readCharacteristic(calibrationDatetimeChar);

    }//readCalibrationDatetime


    private void readCalibrationMode(){

        btGatt.readCharacteristic(calibrationModeChar);

    }//readCalibrationMode


    private void readGestureIndex(){

        btGatt.readCharacteristic(gestureIndexChar);

    }//readGestureIndex


    private void readGestureIteration(){

        btGatt.readCharacteristic(gestureIterationChar);

    }//readGestureIteration


    private void readGestureStatus(){

        btGatt.readCharacteristic(gestureStatusChar);

    }//readGestureStatus


    //write

    private void writeCalibrationDatetime(int _value){

        calibrationDatetimeChar.setValue(_value, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(calibrationDatetimeChar);

    }//writeCalibrationDatetime


    private void writeCalibrationMode(int _mode){

        calibrationModeChar.setValue(_mode, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(calibrationModeChar);

    }//writeCalibrationMode


    private void writeGestureIndex(int _index){

        gestureIndexChar.setValue(_index, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(gestureIndexChar);

    }//writeGestureIndex


    private void writeGestureIteration(int _iteration){

        gestureIterationChar.setValue(_iteration, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(gestureIterationChar);

    }//writeGestureIteration


    private void writeGestureStatus(int _status){

        gestureStatusChar.setValue(_status, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(gestureStatusChar);

    }//writeGestureStatus


    //enable notify

    private void enableCalibrationQualityNotify(boolean _isEnabled){

        btGatt.setCharacteristicNotification(calibrationQualityChar, true);

        BluetoothGattDescriptor cccd = calibrationQualityChar.getDescriptor(CalibrationBleService.CCCD_UUID);

        if(_isEnabled){
            cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else{
            cccd.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        btGatt.writeDescriptor(cccd);

    }//enableCalibrationQualityNotify


    private void enableGestureStatusNotify(boolean _isEnabled){

        btGatt.setCharacteristicNotification(gestureStatusChar, true);

        BluetoothGattDescriptor cccd = gestureStatusChar.getDescriptor(CalibrationBleService.CCCD_UUID);

        if(_isEnabled){
            cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else{
            cccd.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        btGatt.writeDescriptor(cccd);

    }//enableGestureStatusNotify


    //listener read

    public void onCalibrationQualityRead(int _value){}//onCalibrationQualityRead


    public void onCalibrationDatetimeRead(int _value){}//onCalibrationDatetimeRead


    public void onCalibrationModeRead(int _value){}//onCalibrationModeRead


    public void onGestureIndexRead(int _value){}//onGestureIndexRead


    public void onGestureIterationRead(int _value){}//onGestureIterationRead


    public void onGestureStatusRead(int _value){}//onGestureStatusRead


    //listener write

    public void onCalibrationDatetimeWritten(int _value){}//onCalibrationDatetimeWritten


    public void onCalibrationModeWritten(int _value){

        if(_value == CalibrationBleService.CALIBRATION_MODE_HARD){

            calibrationStatus = CalibrationBleService.CALIBRATION_MODE_HARD;

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationStarted();
            }

        }else if(_value == CalibrationBleService.CALIBRATION_MODE_NONE){

            calibrationStatus = CalibrationBleService.CALIBRATION_MODE_NONE;

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationFinished();
            }

        }

    }//onCalibrationModeWritten


    public void onGestureIndexWritten(int _value){

        writeGestureIteration(currentGestureIteration);

    }//onGestureIndexWritten


    public void onGestureIterationWritten(int _value){

        writeGestureStatus(CalibrationBleService.GESTURE_STATUS_STARTED);

    }//onGestureIterationWritten


    public void onGestureStatusWritten(int _value){

        if(_value == CalibrationBleService.GESTURE_STATUS_STARTED){

            gestureStatus = CalibrationBleService.GESTURE_STATUS_STARTED;

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationStepStarted(currentGestureIndex, currentGestureIteration);
            }

        }

    }//onGestureStatusWritten


    //listener enable notify

    public void onCalibrationQualityNotifyEnabled(){}//onCalibrationQualityNotifyEnabled


    public void onCalibrationQualityNotifyDisabled(){}//onCalibrationQualityNotifyDisabled


    public void onGestureStatusNotifyEnabled(){

        for(int i=0 ; i<initListeners.size() ; i++){
            initListeners.get(i).onCalibrationInit();
        }

    }//onGestureStatusNotifyEnabled


    public void onGestureStatusNotifyDisabled(){}//onGestureStatusNotifyDisabled


    //listener characteristics changed

    public void onCalibrationQualityChanged(int _value){}//onCalibrationQualityChanged


    public void onGestureStatusNotifyChanged(int _value){

        if(_value == CalibrationBleService.GESTURE_STATUS_RECORDING){

            gestureStatus = CalibrationBleService.GESTURE_STATUS_RECORDING;

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationStepRecording(currentGestureIndex, currentGestureIteration);
            }

        }else if(_value == CalibrationBleService.GESTURE_STATUS_OK){

            gestureStatus = CalibrationBleService.GESTURE_STATUS_OK;

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationStepDone(currentGestureIndex, currentGestureIteration);
            }

            if(this.currentGestureIndex == this.numGestures && this.currentGestureIteration == this.numIterations){

                stopCalibration();

            }

        }else if(_value == CalibrationBleService.GESTURE_STATUS_ERROR1){

            gestureStatus = CalibrationBleService.GESTURE_STATUS_ERROR1;

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationStepError(currentGestureIndex, currentGestureIteration);
            }

        }

    }//onGestureStatusNotifyChanged


}//CalibrationBleService