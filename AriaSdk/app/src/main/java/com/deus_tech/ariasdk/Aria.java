package com.deus_tech.ariasdk;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.deus_tech.ariasdk.ariaBleService.ArsInitListener;
import com.deus_tech.ariasdk.ariaBleService.AriaBleService;
import com.deus_tech.ariasdk.ble.BluetoothBroadcastListener;
import com.deus_tech.ariasdk.ble.BluetoothBroadcastReceiver;
import com.deus_tech.ariasdk.ble.BluetoothGattCallback;
import com.deus_tech.ariasdk.ble.ConnectionGattListener;
import com.deus_tech.ariasdk.calibrationBleService.CasInitListener;
import com.deus_tech.ariasdk.calibrationBleService.CalibrationBleService;

import java.util.ArrayList;
import java.util.List;


public class Aria extends BroadcastReceiver implements BluetoothBroadcastListener, ConnectionGattListener, CasInitListener, ArsInitListener{


    public final static String DEVICE_NAME = "Aria6";

    public final static int STATUS_NONE = 1;
    public final static int STATUS_DISCOVERING = 2;
    public final static int STATUS_FOUND = 3;
    public final static int STATUS_CONNECTING = 4;
    public final static int STATUS_CONNECTED = 5;
    public final static int STATUS_READY = 6;


    private static Aria instance;
    private Context context;
    //bluetooth
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothBroadcastReceiver btBroadcastReceiver;
    private BluetoothDevice device;
    //gatt
    private BluetoothGatt btGatt;
    private BluetoothGattCallback btGattCallback;
    //listener
    private ArrayList<AriaConnectionListener> listeners;
    //status
    private int status;
    //services
    private CalibrationBleService cas;
    private AriaBleService ars;


    public static Aria getInstance(Context _context){

        if (Aria.instance == null){
            Aria.instance = new Aria(_context);
        }

        return Aria.instance;

    }//getInstance


    public void addListener(AriaConnectionListener _listener){

        listeners.add(_listener);

    }//addListener


    public void removeListener(AriaConnectionListener _listener){

        listeners.remove(_listener);

    }//removeListener


    public CalibrationBleService getCas(){

        return cas;

    }//getCas


    public AriaBleService getArs(){

        return ars;

    }//getArs


    public int getStatus(){

        return status;

    }//getStatus


    public void startDiscovery(){

        device = null;

        if(btAdapter != null && btAdapter.isEnabled() == false){

            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(this, filter);

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableIntent);

        }else{

            btAdapter.startDiscovery();

        }

    }//startDiscovery


    public void stopDiscovery(){

        btAdapter.cancelDiscovery();

    }//stopDiscovery


    public void connect(){

        if(device != null){

            status = Aria.STATUS_CONNECTING;
            btGatt = device.connectGatt(context, false, btGattCallback);

        }

    }//connect


    public void disconnect(){

        if(btGatt != null){
            btGatt.disconnect();
        }

        if(cas != null){
            cas.removeInitListener(this);
        }

        if(ars != null){
            ars.removeInitListener(this);
        }

    }//disconnect


    //private

    private Aria(Context _context){

        context = _context;

        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        initBtBroadcastReceiver();
        initBtGattCallback();

        listeners = new ArrayList<AriaConnectionListener>();
        status = Aria.STATUS_NONE;

    }//constructor


    private void initBtBroadcastReceiver(){

        btBroadcastReceiver = new BluetoothBroadcastReceiver();
        btBroadcastReceiver.setListener(this);

        IntentFilter filter = new IntentFilter();

        //http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
        //filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        //filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //filter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        //filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        //filter.addAction(BluetoothDevice.ACTION_UUID);

        //http://developer.android.com/reference/android/bluetooth/BluetoothAdapter.html
        //filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        //filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        context.registerReceiver(btBroadcastReceiver, filter);

    }//initBtBroadcastReceiver


    private void initBtGattCallback(){

        btGattCallback = new BluetoothGattCallback();
        btGattCallback.setConnectionListener(this);

    }//initBtGattCallback


    //BluetoothBroadcastListener

    public void onReceive(Context context, Intent intent){

        String action = intent.getAction();
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){

            if(state == BluetoothAdapter.STATE_ON){

                context.unregisterReceiver(this);
                btAdapter.startDiscovery();

            }

        }

    }//onReceive


    //ConnectionGattListener

    public void onDiscoveryStarted(){

        status = Aria.STATUS_DISCOVERING;

        for(int i=0 ; i<listeners.size() ; i++){
            listeners.get(i).onDiscoveryStarted();
        }

    }//onDiscoveryStarted


    public void onDiscoveryFinished(){

        if(device == null){
            status = Aria.STATUS_NONE;
        }else{
            status = Aria.STATUS_FOUND;
        }

        for(int i=0 ; i<listeners.size() ; i++){
            listeners.get(i).onDiscoveryFinished(device != null);
        }

        //connect();

    }//onDiscoveryFinished


    public void onDeviceFound(BluetoothDevice _device){

        device = _device;
        stopDiscovery();

    }//onDeviceFound


    public void onDeviceConnected(List<BluetoothGattService> services){

        status = Aria.STATUS_CONNECTED;

        for(int i=0 ; i<listeners.size() ; i++){
            listeners.get(i).onConnected();
        }

        for(int i=0 ; i<services.size() ; i++){

            BluetoothGattService service = services.get(i);

            if(service.getUuid().equals(CalibrationBleService.CALIBRATION_SERVICE_UUID)){

                if(cas != null){
                    cas.removeInitListener(this);
                }

                cas = new CalibrationBleService(context, btGatt, service);
                cas.addInitListener(this);
                btGattCallback.setCalibrationListener(cas);

            }else if(service.getUuid().equals(AriaBleService.ARIA_SERVICE_UUID)){

                if(ars != null){
                    ars.removeInitListener(this);
                }

                ars = new AriaBleService(context, btGatt, service);
                ars.addInitListener(this);
                btGattCallback.setArsListener(ars);

            }

        }

        if(cas != null){
            cas.init();
        }

    }//onDeviceConnected


    public void onDeviceDisconnected(){

        status = Aria.STATUS_NONE;

        for(int i=0 ; i<listeners.size() ; i++){
            listeners.get(i).onDisconnected();
        }

    }//onDeviceDisconnected


    //CasInitListener

    public void onCalibrationInit(){

        ars.init();

    }//onCalibrationInit


    //ArsInitListener

    public void onArsInit(){

        status = Aria.STATUS_READY;

        for(int i=0 ; i<listeners.size() ; i++){
            listeners.get(i).onReady();
        }

    }//onArsInit


}//Aria