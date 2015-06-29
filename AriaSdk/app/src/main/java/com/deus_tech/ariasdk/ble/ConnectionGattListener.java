package com.deus_tech.ariasdk.ble;


import android.bluetooth.BluetoothGattService;

import java.util.List;

public interface ConnectionGattListener{


    void onDeviceConnected(List<BluetoothGattService> _services);

    void onDeviceDisconnected();


}//ConnectionGattListener