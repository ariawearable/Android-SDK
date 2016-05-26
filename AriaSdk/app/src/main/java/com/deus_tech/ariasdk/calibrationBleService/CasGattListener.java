package com.deus_tech.ariasdk.calibrationBleService;


public interface CasGattListener{


    //read

    void onCalibrationAttributeRead(int _value);

    void onCalibrationDatetimeRead(int _value);

    void onCalibrationModeRead(int _value);

    void onSettingsCommandRead(int _value);

    void onSettingsDataRead(int _value);

    void onGestureStatusRead(int _value);


    //written

    void onCalibrationDatetimeWritten(int _value);

    void onCalibrationModeWritten(int _value);

    void onSettingsCommandWritten(int _value);

    void onSettingsDataWritten(int _value);

    void onGestureStatusWritten(int _value);


    //enable notify

    void onCalibrationAttributeNotifyEnabled();

    void onCalibrationAttributeNotifyDisabled();

    void onGestureStatusNotifyEnabled();

    void onGestureStatusNotifyDisabled();


    //receive notify

    void onCalibrationAttributeChanged(int _value);

    void onGestureStatusNotifyChanged(int _value);


}//CasGattListener
