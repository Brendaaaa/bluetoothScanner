package com.scan.out;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by brendaramires on 19/01/16.
 */
public class BluetoothService extends Service {

    private static final long MINUTE = 1000 * 60;

    @Override
    public void onCreate() {
        System.out.println("fui criado");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("ta comecando");

        BluetoothApplication app = (BluetoothApplication)getApplication();
        BluetoothHandler bluetoothHandler = app.getBluetoothHandler();
        bluetoothHandler.setOnScanListener(new BluetoothHandler.OnScanListener() {
            @Override
            public void onScanFinished() {
                stopSelf();
            }

            @Override
            public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            }
        });
        bluetoothHandler.scanLeDevice(true);


        // I don't want this service to stay in memory, so I stop it
        // immediately after doing what I wanted it to do.


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // I want to restart this service again in one hour
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.set(
                alarm.RTC_WAKEUP,
                System.currentTimeMillis() + (MINUTE/2),
                PendingIntent.getService(this, 0, new Intent(this, BluetoothService.class), 0)
        );
    }
}
