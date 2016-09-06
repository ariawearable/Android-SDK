package com.deus_tech.ariasdk.calibrationBleService;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.deus_tech.aria.CasEvents.CalibrationAttributeEvent;
import com.deus_tech.aria.CasEvents.OnCalibrationWritten;
import com.deus_tech.aria.CasEvents.GestureStatusEvent;
import com.deus_tech.ariasdk.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.UUID;

public class CalibrationBleService implements CasGattListener{
    private String TAG="CalibrationBleService";
    //UUID-s
    public final static UUID CALIBRATION_SERVICE_UUID = UUID.fromString("caa50000-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID CALIBRATION_ATTRIBUTE_UUID = UUID.fromString("caa50001-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID CALIBRATION_ERROR_UUID = UUID.fromString("caa50002-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID CALIBRATION_MODE_UUID = UUID.fromString("caa50003-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID SETTINGS_COMMAND_UUID = UUID.fromString("caa50004-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID SETTINGS_DATA_UUID = UUID.fromString("caa50005-2244-a09d-e968-5f43e74d0c5c");
    public final static UUID GESTURE_STATUS_UUID = UUID.fromString("caa50006-2244-a09d-e968-5f43e74d0c5c");



    //settings command values

    public final static int SET_NUMBER_GESTURE = 0xC0;
    public final static int SET_NUMBER_REPETITION = 0xC1;

    //calibration attribute values

    public final static int CALIBRATION_IS_PRESENT = 1;
    public final static int CALIBRATION_IS_NOT_PRESENT = 0;


    //calibration mode values

    public final static int CALIBRATION_MODE_NONE = 0;
    public final static int STATUS_CALIB = 1;
    public final static int STATUS_EXEC = 2;
    public final static int STATUS_SLEEP = 3;
    public final static int STATUS_IDLE = 0;
    public final static int STATUS_PRECALIB_AMP = 4;
    public final static int STATUS_PRECALIB_CAD = 5;
    public final static int STATUS_PRECALIB_SIM = 6;
    public final static int STATUS_PRECALIB_DEB = 7;

    //gesture status values
    public final static int GESTURE_STATUS_NONE = 0;
    public final static int GESTURE_STATUS_STARTED = 1;
    public final static int GESTURE_STATUS_RECORDING = 2;
    public final static int GESTURE_STATUS_OK = 3;
    public final static int GESTURE_STATUS_ERROR1 = 4;
    public final static int GESTURE_STATUS_ERROR2 = 5;
    public final static int GESTURE_STATUS_ERROR3 = 6;
    public final static int GESTURE_STATUS_OKREPETITION = 7;
    public final static int GESTURE_STATUS_OKGESTURE = 8;
    public final static int GESTURE_STATUS_OKCALIBRATION = 9;
    public final static int GESTURE_STATUS_OKCAMP = 10;
    public final static int GESTURE_STATUS_OKCAD = 11;
    public final static int GESTURE_STATUS_OKCSIM = 12;

    public final static int OLD_PROTOCOL = 0;
    public final static int NEW_PROTOCOL = 1;

    private Context context;
    //bluetooth
    private BluetoothGatt btGatt;
    private BluetoothGattService btGattService;
    //characteristics
    private BluetoothGattCharacteristic calibrationAttributeChar;
    private BluetoothGattCharacteristic calibrationErrorChar;
    private BluetoothGattCharacteristic calibrationModeChar;
    private BluetoothGattCharacteristic settingsCommandChar;
    private BluetoothGattCharacteristic settingsDataChar;
    private BluetoothGattCharacteristic gestureStatusChar;
    //listeners
    private ArrayList<CasInitListener> initListeners;
    private ArrayList<CasListener> casListeners;
    //calibration
    private int gestureProtocol;
    private int numGestures;
    private int numRepetitions;
    private int calibrationStatus;
    private int gestureStatus;
    private int currentGestureIndex;
    private int currentGestureIteration;
    private int settingsCommand;
    private int settingsData;

    public CalibrationBleService(Context _context, BluetoothGatt _btGatt, BluetoothGattService _btGattService){

        context = _context;

        numGestures = context.getResources().getInteger(R.integer.calibration_gestures);
        numRepetitions = context.getResources().getInteger(R.integer.calibration_iterations);

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
		// there should be only single listener so it will clear
        // previous one before adding new
        casListeners.clear();
        casListeners.add(_listener);

    }//addCasListener


    public void removeCasListener(CasListener _listener){

        casListeners.remove(_listener);

    }//removeCasListener


    public void init(){

        calibrationStatus = CalibrationBleService.CALIBRATION_MODE_NONE;
        gestureStatus = CalibrationBleService.GESTURE_STATUS_NONE;
        gestureProtocol=CalibrationBleService.NEW_PROTOCOL;

        calibrationAttributeChar = btGattService.getCharacteristic(CalibrationBleService.CALIBRATION_ATTRIBUTE_UUID);
        calibrationErrorChar = btGattService.getCharacteristic(CalibrationBleService.CALIBRATION_ERROR_UUID);
        calibrationModeChar = btGattService.getCharacteristic(CalibrationBleService.CALIBRATION_MODE_UUID);
        settingsCommandChar = btGattService.getCharacteristic(CalibrationBleService.SETTINGS_COMMAND_UUID);
        settingsDataChar = btGattService.getCharacteristic(CalibrationBleService.SETTINGS_DATA_UUID);
        gestureStatusChar = btGattService.getCharacteristic(CalibrationBleService.GESTURE_STATUS_UUID);

        //Once CalibrationAttribute gets active, the callback onCalibrationAttributeNotifyEnabled launches enableGestureStatusNotify(true);
        enableCalibrationAttributeNotify(true);


    }//init




    public int getGesturesNumber(){

        return numGestures;

    }//getGesturesNumber

    public void setGesturesNumber(int val){

        numGestures=val;

    }

    public int getIterationsNumber(){

        return numRepetitions;

    }//getIterationsNumber

    public void setIterationsNumber(int val){

        numRepetitions =val;

    }//getIterationsNumber


    public void startCalibration(){

        calibrationStatus = CalibrationBleService.CALIBRATION_MODE_NONE;
        gestureStatus = CalibrationBleService.GESTURE_STATUS_NONE;
        currentGestureIndex = 1;
        currentGestureIteration = 1;

        //deprecated writeCalibrationMode(CalibrationBleService.CALIBRATION_MODE_HARD);
        writeStatus_Calib();

    }//startCalibration


    public void nextCalibrationStep(){

        if ( gestureProtocol == NEW_PROTOCOL ) writeGestureStatus(GESTURE_STATUS_STARTED);
        else {
            currentGestureIteration++;
            Log.d(TAG, "nextCalibrationStep: Iter " + Integer.toString(currentGestureIteration));
            if (currentGestureIteration > numRepetitions) {

                currentGestureIndex++;
                currentGestureIteration = 1;

            }

            if (currentGestureIndex > numGestures) {

                for (int i = 0; i < casListeners.size(); i++) {
                    casListeners.get(i).onCalibrationFinished();
                }

            } else {

                // deprecated writeGestureIndex(currentGestureIndex);
                writeGestureStatus(CalibrationBleService.GESTURE_STATUS_STARTED);


            }

        }


    }//nextCalibrationStep

    public void repeatCalibrationStep(){

        //writeGestureIndex(this.currentGestureIndex);

    }//repeatCalibrationStep


    public void stopCalibration(){
        //TODO: implement the control on the Calibration_Attribute
        writeStatus_Exec();


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

    public void readCalibrationAttribute(){
        Log.d("debug_ble", "readCalibrationAttribute");
        btGatt.readCharacteristic(calibrationAttributeChar);

    }//readCalibrationAttribute


    public void readCalibrationError(){

        btGatt.readCharacteristic(calibrationErrorChar);

    }//readCalibrationDatetime


    private void readCalibrationMode(){

        btGatt.readCharacteristic(calibrationModeChar);

    }//readCalibrationMode


    private void readGestureIndex(){

        btGatt.readCharacteristic(settingsCommandChar);

    }//readGestureIndex


    private void readGestureIteration(){

        btGatt.readCharacteristic(settingsDataChar);

    }//readGestureIteration


    public void readGestureStatus(){

        btGatt.readCharacteristic(gestureStatusChar);

    }//readGestureStatus


    //write

    private void writeCalibrationDatetime(int _value){

        calibrationErrorChar.setValue(_value, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(calibrationErrorChar);

    }//writeCalibrationDatetime


    private void writeCalibrationMode(int _mode){
        calibrationStatus = _mode;
        calibrationModeChar.setValue(_mode, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(calibrationModeChar);

    }//writeCalibrationMode

    public void writeStatus_Exec(){
        writeCalibrationMode(STATUS_EXEC);
    }

    public void writeStatus_Sleep(){
        writeCalibrationMode(STATUS_SLEEP);
    }

    public void writeStatus_Calib(){
        writeCalibrationMode(STATUS_CALIB);
    }

    public void writeStatus_Idle(){
        writeCalibrationMode(STATUS_IDLE);
    }
    public void writeStatus_Pre_Amp(){
        writeCalibrationMode(STATUS_PRECALIB_AMP);
    }
    public void writeStatus_Pre_Cad(){
        writeCalibrationMode(STATUS_PRECALIB_CAD);
    }
    public void writeStatus_Pre_Deb(){
        writeCalibrationMode(STATUS_PRECALIB_DEB);
    }
    public void writeStatus_Pre_Sim(){
        writeCalibrationMode(STATUS_PRECALIB_SIM);
    }

    public void writeSettingsCommand(int _command){
        settingsCommand=_command;  //USE this for the callback onSettingsCommandWritten();
        settingsCommandChar.setValue(_command, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(settingsCommandChar);

    }//writeGestureIndex


    public void writeSettingsData(int _data){
        settingsData = _data;   //USE this for the callback onSettingsDataWritten();
        settingsDataChar.setValue(_data, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(settingsDataChar);

    }//writeGestureIteration


    private void writeGestureStatus(int _status){

        gestureStatusChar.setValue(_status, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(gestureStatusChar);

    }//writeGestureStatus

    public void writeGestureStatusStart(){

        gestureStatusChar.setValue(GESTURE_STATUS_STARTED, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        btGatt.writeCharacteristic(gestureStatusChar);

    }//writeGestureStatus


    //enable notify

    public void enableCalibrationAttributeNotify(boolean _isEnabled){

        btGatt.setCharacteristicNotification(calibrationAttributeChar, true);

        BluetoothGattDescriptor cccd = calibrationAttributeChar.getDescriptor(CalibrationBleService.CCCD_UUID);

        if(_isEnabled){
            cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else{
            cccd.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        btGatt.writeDescriptor(cccd);

    }//enableCalibrationAttributeNotify


    private void enableCalibrationModeNotify(boolean _isEnabled){

        btGatt.setCharacteristicNotification(calibrationModeChar, true);

        BluetoothGattDescriptor cccd = calibrationModeChar.getDescriptor(CalibrationBleService.CCCD_UUID);

        if(_isEnabled){
            cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else{
            cccd.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        btGatt.writeDescriptor(cccd);

    }//enableCalibrationModeNotify


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

    public void onCalibrationAttributeRead(int _value){
        Log.d("debug_ble", "onCalibrationAttributeRead");
    }//onCalibrationAttributeRead


    public void onCalibrationDatetimeRead(int _value){}//onCalibrationDatetimeRead


    public void onCalibrationModeRead(int _value){}//onCalibrationModeRead


    public void onSettingsCommandRead(int _value){}//onGestureIndexRead


    public void onSettingsDataRead(int _value){}//onGestureIterationRead


    public void onGestureStatusRead(int _value){
        gestureStatus=_value;
        EventBus.getDefault().post(new GestureStatusEvent(_value));
    }//onGestureStatusRead



    //listener write

    public void onCalibrationDatetimeWritten(int _value){
        Log.d(TAG, "onCalibrationDatetimeWritten: ");
    }//onCalibrationDatetimeWritten


    public void onCalibrationModeWritten(int _value){
        EventBus.getDefault().post(new OnCalibrationWritten(_value));
        Log.d(TAG, "onCalibrationModeWritten: calibration mode scritto correttamente" + _value);
        calibrationStatus=_value;
        if(_value == CalibrationBleService.STATUS_CALIB){

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationStarted();
            }

        }
        if(_value == CalibrationBleService.STATUS_EXEC){
        }

//        else if(_value == CalibrationBleService.STATUS_EXEC){
//
//            calibrationStatus = CalibrationBleService.STATUS_EXEC;
//
//            for(int i=0 ; i<casListeners.size() ; i++){
//                casListeners.get(i).onCalibrationFinished();
//            }
//
//        }

    }//onCalibrationModeWritten


    public void onSettingsCommandWritten(int _value){
        Log.d(TAG, "onSettingsCommandWritten: ");
        if      (settingsCommand==SET_NUMBER_GESTURE) writeSettingsData(numGestures);
        else if (settingsCommand==SET_NUMBER_REPETITION)   writeSettingsData(numRepetitions);

    }//onGestureIndexWritten


    public void onSettingsDataWritten(int _value){
        Log.d(TAG, "onSettingsDataWritten: ");
//        DON'T use the callback like onSettingsCommandWritten() because the data is not mutually different among commands.

//        if (settingsCommand == SET_NUMBER_GESTURE )
//        {   settingsCommand = SET_NUMBER_REPETITION;
//            writeSettingsCommand(settingsCommand);
//        }

        //deprecated writeGestureStatus(CalibrationBleService.GESTURE_STATUS_STARTED);

    }//onGestureIterationWritten


    public void onGestureStatusWritten(int _value){
        Log.d(TAG, "onGestureStatusWritten: ");

        if(_value == CalibrationBleService.GESTURE_STATUS_STARTED){

            gestureStatus = CalibrationBleService.GESTURE_STATUS_STARTED;

            for(int i=0 ; i<casListeners.size() ; i++){
                casListeners.get(i).onCalibrationStepStarted(currentGestureIndex, currentGestureIteration);
            }

        }

    }//onGestureStatusWritten


    //listener enable notify

    public void onCalibrationAttributeNotifyEnabled(){
        Log.d("debug_ble", "onCalibrationAttributeNotifyEnabled");
        enableGestureStatusNotify(true);

    }//onCalibrationAttributeNotifyEnabled


    public void onCalibrationAttributeNotifyDisabled(){}//onCalibrationAttributeNotifyDisabled


    public void onGestureStatusNotifyEnabled(){
        Log.d("debug_ble", "onGestureStatusNotifyEnabled");
        for(int i=0 ; i<initListeners.size() ; i++){
            initListeners.get(i).onCalibrationInit();
        }

    }//onGestureStatusNotifyEnabled


    public void onGestureStatusNotifyDisabled(){}//onGestureStatusNotifyDisabled


    //listener characteristics changed

    public void onCalibrationAttributeChanged(int _value){
        EventBus.getDefault().post(new CalibrationAttributeEvent(_value));
        Log.d("ble_debug", "onCalibrationAttributeChanged");
        //readCalibrationAttribute();

    }//onCalibrationAttributeChanged



    public void onCalibrationModeChanged(int _value){

    }//onCalibrationModeChanged

    public void onGestureStatusNotifyChanged(int _value){
        Log.d("ble_debug", "onGestureStatusNotifyChanged " + Integer.toString(_value));

        EventBus.getDefault().post(new GestureStatusEvent(_value));

        /*
        if (calibrationStatus==STATUS_CALIB) {
            _value=_value%10;
            if (_value == CalibrationBleService.GESTURE_STATUS_RECORDING) {
                //  if (gestureStatus==CalibrationBleService.GESTURE_STATUS_STARTED) { //check it receives the started before recording
                gestureStatus = CalibrationBleService.GESTURE_STATUS_RECORDING;

                for (int i = 0; i < casListeners.size(); i++) {
                    casListeners.get(i).onCalibrationStepRecording(currentGestureIndex, currentGestureIteration);
                }
                // }
            } else if (_value == CalibrationBleService.GESTURE_STATUS_OK) {
                // if (gestureStatus ==  CalibrationBleService.GESTURE_STATUS_RECORDING) { //check it receives recording before ok gesture
                Log.d(TAG, "onGestureStatusNotifyChanged: Stiamo usando il VECCHIO protocollo");

                gestureProtocol = OLD_PROTOCOL;
                gestureStatus = CalibrationBleService.GESTURE_STATUS_OK;


                for (int i = 0; i < casListeners.size(); i++) {
                    casListeners.get(i).onCalibrationStepDone(currentGestureIndex, currentGestureIteration);
                }

//                if (this.currentGestureIndex == this.numGestures+1 && this.currentGestureIteration == this.numRepetitions) {
//
//                    stopCalibration();
//
//
//                }
                //}

            } else if (_value == CalibrationBleService.GESTURE_STATUS_ERROR1) {

                this.currentGestureIteration = 1;
                gestureStatus = CalibrationBleService.GESTURE_STATUS_ERROR1;

                for (int i = 0; i < casListeners.size(); i++) {
                    casListeners.get(i).onCalibrationStepError(currentGestureIndex, currentGestureIteration);
                }

            } else if (_value == CalibrationBleService.GESTURE_STATUS_ERROR2) {

                this.currentGestureIteration = 0;
                gestureStatus = CalibrationBleService.GESTURE_STATUS_ERROR2;

                for (int i = 0; i < casListeners.size(); i++) {
                    casListeners.get(i).onCalibrationStepError(currentGestureIndex, currentGestureIteration);
                }

            } else if (_value == CalibrationBleService.GESTURE_STATUS_OKREPETITION) {
                Log.d(TAG, "onGestureStatusNotifyChanged: Stiamo usando il nuovo protocollo");

                //   if (gestureStatus ==  CalibrationBleService.GESTURE_STATUS_RECORDING) { //check it receives recording before ok gesture
                gestureStatus = CalibrationBleService.GESTURE_STATUS_OKREPETITION;
                currentGestureIteration++;
                gestureProtocol = NEW_PROTOCOL;


                for (int i = 0; i < casListeners.size(); i++) {
                    casListeners.get(i).onCalibrationStepDone(currentGestureIndex, currentGestureIteration);
                }
                //    }
            } else if (_value == CalibrationBleService.GESTURE_STATUS_OKGESTURE) {
                //   if (gestureStatus ==  CalibrationBleService.GESTURE_STATUS_RECORDING) { //check it receives recording before ok gesture
                Log.d(TAG, "onGestureStatusNotifyChanged: Stiamo usando il nuovo protocollo");
                gestureProtocol = NEW_PROTOCOL;

                gestureStatus = CalibrationBleService.GESTURE_STATUS_OKGESTURE;
                currentGestureIndex++;

                currentGestureIteration = 1;


                for (int i = 0; i < casListeners.size(); i++) {
                    casListeners.get(i).onCalibrationStepDone(currentGestureIndex, currentGestureIteration);
                }

                //    }

            } else if (_value == CalibrationBleService.GESTURE_STATUS_OKCALIBRATION) {
                gestureProtocol = NEW_PROTOCOL;

                gestureStatus = CalibrationBleService.GESTURE_STATUS_OKCALIBRATION;

                stopCalibration();

                for (int i = 0; i < casListeners.size(); i++) {
                    casListeners.get(i).onCalibrationFinished();
                }


            }
        }else if(calibrationStatus == STATUS_PRECALIB_AMP){
            Log.d("ble_debug", "onGestureStatusNotifyChanged and in PRECALIB_AMP " + Integer.toString(_value));

        }*/


    }//onGestureStatusNotifyChanged


    public void setGesture(int value){
        setGesturesNumber(value);
        writeSettingsCommand(SET_NUMBER_GESTURE);
    }

    public void setRepetition(int value){
        setIterationsNumber(value);
        writeSettingsCommand(SET_NUMBER_REPETITION);
    }

}//CalibrationBleService