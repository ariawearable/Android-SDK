package com.deus_tech.ariasdk.calibrationBleService;


public interface CasGattListener{


    //read

    void onCalibrationQualityRead(int _value);

    void onCalibrationDatetimeRead(int _value);

    void onCalibrationModeRead(int _value);

    void onGestureIndexRead(int _value);

    void onGestureIterationRead(int _value);

    void onGestureStatusRead(int _value);


    //written

    void onCalibrationDatetimeWritten(int _value);

    void onCalibrationModeWritten(int _value);

    void onGestureIndexWritten(int _value);

    void onGestureIterationWritten(int _value);

    void onGestureStatusWritten(int _value);


    //enable notify

    void onCalibrationQualityNotifyEnabled();

    void onCalibrationQualityNotifyDisabled();

    void onGestureStatusNotifyEnabled();

    void onGestureStatusNotifyDisabled();


    //receive notify

    void onCalibrationQualityChanged(int _value);

    void onGestureStatusNotifyChanged(int _value);


}//CasGattListener
