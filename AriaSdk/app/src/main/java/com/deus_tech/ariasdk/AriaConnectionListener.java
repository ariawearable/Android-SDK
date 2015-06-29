package com.deus_tech.ariasdk;


public interface AriaConnectionListener{


    void onDiscoveryStarted();

    void onDiscoveryFinished(boolean _found);

    void onConnected();

    void onReady();

    void onDisconnected();


}//AriaConnectionListener
