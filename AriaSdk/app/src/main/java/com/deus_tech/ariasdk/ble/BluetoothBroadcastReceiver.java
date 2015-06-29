package com.deus_tech.ariasdk.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deus_tech.ariasdk.Aria;


public class BluetoothBroadcastReceiver extends BroadcastReceiver{


    private BluetoothBroadcastListener listener;
    private boolean discovering = false;


    public void setListener(BluetoothBroadcastListener _listener){

        listener = _listener;

    }//setListener


    public void onReceive(Context context, Intent intent){

        String action = intent.getAction();

        if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){

            discovering = true;

            if(listener!=null){
                listener.onDiscoveryStarted();
            }

        }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){

            if(discovering == false) return;

            discovering = false;

            if(listener!=null){
                listener.onDiscoveryFinished();
            }

        }else if(action.equals(BluetoothDevice.ACTION_FOUND)){

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(device.getName() == null || device.getName().equals(Aria.DEVICE_NAME) == false) return;

            if(listener != null){
                listener.onDeviceFound(device);
            }

        }else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){

            //Indicates a change in the bond state of a remote device. For example, if a device is bonded (paired).
            //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            //int oldState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
            //Log.d("debug", "[BluetoothDevice] bond state changed---------- " + device.getName() + " new: " + state + " old: " + oldState);

        }

    }//onReceive


}//BluetoothBroadcastReceiver