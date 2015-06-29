package com.deus_tech.ariasdk.ariaBleService;


public interface ArsGattListener{


    void onGestureNotifyEnabled();

    void onGestureNotifyDisabled();

    void onGestureChanged(int _value);


    void onBatteryRead(int _value);

    void onBatteryNotifyEnabled();

    void onBatteryNotifyDisabled();

    void onBatteryChanged(int _value);


}//ArsGattListener
