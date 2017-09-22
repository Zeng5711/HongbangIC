package com.hongbang.ic.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.hongbang.ic.IBluetoothICService;
import com.hongbang.ic.util.Logger;

public class BluetoothICService extends Service {

    private class IBluetoothICServiceImpl extends IBluetoothICService.Stub {

        @Override
        public void makeDiscoverable() throws RemoteException {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
//            discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(discoverableIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IBluetoothICServiceImpl();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.debug("onStartCommand: " + flags);
        Logger.debug("hashcode: " + this);
        return START_STICKY;
    }
}
