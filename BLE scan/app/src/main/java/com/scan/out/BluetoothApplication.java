package com.scan.out;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Created by brendaramires on 19/01/16.
 */
public class BluetoothApplication extends Application {

    BluetoothHandler b;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        b = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public BluetoothHandler getBluetoothHandler(){
        return b;
    }

    public void setBluetoothHandler(BluetoothHandler b){
        this.b = b;
    }
}
