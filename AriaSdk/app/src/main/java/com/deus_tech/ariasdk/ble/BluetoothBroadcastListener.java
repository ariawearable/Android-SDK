package com.deus_tech.ariasdk.ble;


import android.bluetooth.BluetoothDevice;

public interface BluetoothBroadcastListener{


    void onDiscoveryStarted();

    void onDiscoveryFinished();

    void onDeviceFound(BluetoothDevice _device);


}//BluetoothBroadcastListener
