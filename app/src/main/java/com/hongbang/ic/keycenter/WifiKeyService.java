package com.hongbang.ic.keycenter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
//import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hongbang.ic.IWifiKeyService;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.keycenter.tasks.AbstractTask;
import com.hongbang.ic.keycenter.tasks.OpenDoorTask;
import com.hongbang.ic.keycenter.tasks.OnTaskCallback;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.util.Logger;
import com.hongbang.ic.util.NotificationUtils;
import com.hongbang.ic.util.SPUtils;
import com.hongbang.ic.util.T;
import com.squareup.seismic.ShakeDetector;

import org.xutils.x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import permissions.dispatcher.PermissionUtils;

import static com.hongbang.ic.util.SPUtils.KEY_OPEN_DOOR_TIME;

public class WifiKeyService extends Service implements ShakeDetector.Listener {

    private final IWifiKeyService.Stub mBinder = new IWifiKeyService.Stub() {
        @Override
        public void reloadKeyData() throws RemoteException {
            loadKeyData();
        }

        @Override
        public void setShakeEnabled(boolean enabled) throws RemoteException {
            isShakeEnabled = enabled;
            if (enabled) {
                enableShakeListener();
            } else {
                disableShakeListener();
            }
        }

        @Override
        public void onCommunityChanged() throws RemoteException {
            UserDefaults.defaults().reload();
            loadKeyData();
        }


        @Override
        public void setEnabled(boolean enabled) throws RemoteException {
            isEnabled = enabled;
            if (!enabled && mCurrentTask != null && mCurrentTask.isWorking()) {
                mCurrentTask.destroy();
            }
        }

        @Override
        public void setScreenOnEnabled(boolean enabled) throws RemoteException {
            isScreenOnEnabled = enabled;
        }

        @Override
        public void setAutoConn(boolean enabled) throws RemoteException {
            isAutoConn = enabled;
        }
    };

    private List<OneKeyInfo4DB> mKeyList;

    private OpenDoorTask mCurrentTask = null;

    private Vibrator mVibrator;
    private ShakeDetector mShakeDetector;

    private boolean isScreenUnlocked = true;

    private boolean isEnabled = true;

    private ArrayBlockingQueue<String> mWifiStateQueue = new ArrayBlockingQueue<>(50);

    private boolean isScreenOnEnabled = false;

    private boolean isShakeEnabled = false;

    private WifiAdmin mWifiAdmin;

    private static final int HANDLER_CLEAR_KEYS = 1;

    private static final int HANDLER_SCAN_WIFI = 2;

    private boolean isAutoConn = true;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_CLEAR_KEYS:
                    KeyUtils.clearTempKeys();
                    loadKeyData();
                    mHandler.sendEmptyMessageDelayed(HANDLER_CLEAR_KEYS, 600 * 1000);
                    break;
                case HANDLER_SCAN_WIFI:
                    Object value = SPUtils.get(WifiKeyService.this, KEY_OPEN_DOOR_TIME, -1L);
                    if (value != null) {
                        long time = (Long) value;
                        if (System.currentTimeMillis() - time < 4000) {
                            mHandler.sendEmptyMessageDelayed(HANDLER_SCAN_WIFI, Math.max(3 * 1000, System.currentTimeMillis() - time));
                            return;
                        }
                    }
                    if(isAutoConn) {
                        startScanWifi();
                    }
//                    startBrushTask();
                    mHandler.sendEmptyMessageDelayed(HANDLER_SCAN_WIFI, 3 * 1000);
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mWifiAdmin = new WifiAdmin(this);

        isScreenOnEnabled = (boolean) SPUtils.get(this, SPUtils.KEY_SCREEN_ON_ENABLED, true);
        isShakeEnabled = (boolean) SPUtils.get(this, SPUtils.KEY_SHAKE_ENABLED, true);
        isAutoConn = (boolean) SPUtils.get(this, SPUtils.KEY_AUT_CONN, true);
        loadKeyData();

        mHandler.sendEmptyMessage(HANDLER_CLEAR_KEYS);

        mHandler.sendEmptyMessage(HANDLER_SCAN_WIFI);

        //扫描的WiFi
        registerScreenReceiver();
        registerWifiReceiver();
        registerUserinfoReceiver();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mScreenReceiver);
        unregisterReceiver(mWifiConnectReceiver);
        unregisterReceiver(mUserInfoReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    private void startBrushTask() {
        if (!checkPermission()) {
            x.task().post(new Runnable() {
                @Override
                public void run() {
                    T.showShort(x.app(), "没有定位权限，无法执行刷卡操作。");
                }
            });
            return;
        }

        if (mCurrentTask != null && mCurrentTask.isWorking()) {
            return;
        }
        if (!mWifiAdmin.isWifiEnabled() && !mWifiAdmin.isWifiEnabling()
                && (!isScreenUnlocked || !mWifiAdmin.openWifi())) {
            return;
        }
        mCurrentTask = new OpenDoorTask(WifiKeyService.this);
        isSelectColse = false;
        mCurrentTask.setSelectColse(isSelectColse);
        mCurrentTask.setOnTaskCallback(mBrushCardCallback);
        mCurrentTask.setWifiStateQueue(mWifiStateQueue);
        if (mWifiAdmin.isWifiEnabled()) {
            mCurrentTask.start();
        }
    }

    private void setListeners() {
        isScreenOnEnabled = (boolean) SPUtils.get(WifiKeyService.this, SPUtils.KEY_SCREEN_ON_ENABLED, true);
        isShakeEnabled = (boolean) SPUtils.get(WifiKeyService.this, SPUtils.KEY_SHAKE_ENABLED, true);
        if (isShakeEnabled) {
            enableShakeListener();
        } else {
            disableShakeListener();
        }

    }

    private boolean checkPermission() {
        return PermissionUtils.hasSelfPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void startScanWifi() {
        if (!checkPermission()) {
            return;
        }

        if (mCurrentTask != null && mCurrentTask.isWorking()) {
            return;
        }

        if (!isEnabled || mKeyList == null || mKeyList.size() == 0) {
            return;
        }

        if (!mWifiAdmin.isWifiEnabled() && !mWifiAdmin.isWifiEnabling()) {
            return;
        }


        mWifiAdmin.startScan();
        List<ScanResult> list = mWifiAdmin.getWifiList();
        List<ScanResult> newList = new ArrayList<ScanResult>();
        for (ScanResult wifi : list) {
            if (checkSSID(wifi.SSID)) {
                newList.add(wifi);
            }
        }


        if (null != newList && newList.size() > 1) {
            Collections.sort(newList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult o1, ScanResult o2) {
                    int one = WifiManager.calculateSignalLevel(o1.level, 100);
                    int tow = WifiManager.calculateSignalLevel(o2.level, 100);
                    if (one < tow) {
                        return 1;
                    }
                    if (one == tow) {
                        return 0;
                    }
                    return -1;
                }
            });


            WifiInfo wifiInfo = mWifiAdmin.getWifiInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                if (ssid != null && ssid.length() >= 9) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                    if (checkSSID(ssid) && newList.get(0).SSID.equals(ssid)) {
                        return;
                    }
                }
            }

            connectWIFI(newList.get(0));
        } else if (null != newList && newList.size() == 1) {

            WifiInfo wifiInfo = mWifiAdmin.getWifiInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                if (ssid != null && ssid.length() >= 9) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                    if (checkSSID(ssid) && newList.get(0).SSID.equals(ssid)) {
                        return;
                    }
                }
            }

            connectWIFI(newList.get(0));
        }

    }


    private void connectWIFI(ScanResult wifi) {
        final String ssid = wifi.SSID;


//        String oldSSID = mWifiAdmin.getSSID();
//        if (oldSSID != null && oldSSID.length() >= 9) {
//            oldSSID = oldSSID.substring(1, oldSSID.length() - 1);
//            if (checkSSID(oldSSID)) {
//                mWifiAdmin.disconnectWifi(oldSSID);
//            }
//        }


        if (mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(ssid, AbstractTask.WIFI_PASSWORD, 3))) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mWifiStateQueue != null) {
                        try {
                            mWifiStateQueue.clear();
                            String state = mWifiStateQueue.poll(3, TimeUnit.SECONDS);
                            if ("connected".equals(state)) {
                                NotificationUtils.sendNotification(getApplicationContext(),
                                        ssid.endsWith("000") ? "已连接到发卡器" : "已连接到刷卡器", "设备编号：" + ssid, true);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }
//            break;
    }


    private boolean checkSSID(String ssid) {
        return UserDefaults.defaults().getUserInfo() != null && ssid != null
                && ssid.matches("^" + UserDefaults.defaults().getUserInfo().communityId + "-\\d{3}$");
    }

    private void enableShakeListener() {
        if (mShakeDetector == null) {
            mShakeDetector = new ShakeDetector(this);
        }
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
//        mShakeDetector.setSensitivity(100);
        mShakeDetector.start(sm);
    }

    private void disableShakeListener() {
        if (mShakeDetector != null) {
            mShakeDetector.stop();
        }
    }

    private long mLastShakeTime = -1;
    private static final int SHAKE_INTERVAL = 2000;

    @Override
    public void hearShake() {
        long current = System.currentTimeMillis();
        if (mLastShakeTime > -1 && current - mLastShakeTime < SHAKE_INTERVAL) {
            return;
        }
        mLastShakeTime = current;
        if (isShakeEnabled) {
//            startVibrate();
//            SystemClock.sleep(500);
//            startVibrate();

            startBrushTask();
        }
    }

    private void loadKeyData() {
        mKeyList = KeyUtils.getAll();
        setListeners();
    }

    private void startVibrate() {
        if (mVibrator != null && mVibrator.hasVibrator()) {
            mVibrator.vibrate(500);
        }
    }

    private static void showToast(final String message) {
        x.task().post(new Runnable() {
            @Override
            public void run() {
                T.showShort(x.app(), message);
            }
        });
    }

    private ScreenBroadcastReceiver mScreenReceiver;

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;


        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            isScreenUnlocked = Intent.ACTION_USER_PRESENT.equals(action);

            if (isScreenOnEnabled && Intent.ACTION_SCREEN_ON.equals(action)) {
                startBrushTask();
            }
        }
    }

    private void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mScreenReceiver = new ScreenBroadcastReceiver();
        registerReceiver(mScreenReceiver, filter);
    }

    private void registerWifiReceiver() {
        IntentFilter mWifiFilter = new IntentFilter();
        mWifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiConnectReceiver, mWifiFilter);
    }

    private BroadcastReceiver mWifiConnectReceiver = new BroadcastReceiver() {

        private int mWifiState;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                mWifiState = wifiState;
                Logger.info("WifiManager", "WIFI_STATE_CHANGED_ACTION: " + mWifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        if (mCurrentTask != null && mCurrentTask.hasStarted()) {
                            mCurrentTask.destroy();
                        }
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        if (mCurrentTask != null && mCurrentTask.hasStarted()) {
                            mCurrentTask.destroy();
                        }
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        if (mCurrentTask != null && !mCurrentTask.hasStarted()) {
                            mCurrentTask.start();
                        }
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        break;
                }
            }
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;
                    Logger.info("WifiManager", "NETWORK_STATE_CHANGED_ACTION: " + isConnected);
                    if (isConnected && mWifiState == WifiManager.WIFI_STATE_ENABLED ||
                            isConnected && mWifiAdmin != null ? mWifiAdmin.isWifiEnabled() : false) {
                        mWifiStateQueue.clear();
                        mWifiStateQueue.add("connected");
                    }
                }
            }
        }
    };

    public static final String USER_INFO_CHANGED_ACTION = "user_info_changed_action";

    private void registerUserinfoReceiver() {
        IntentFilter mWifiFilter = new IntentFilter();
        mWifiFilter.addAction(USER_INFO_CHANGED_ACTION);
        registerReceiver(mUserInfoReceiver, mWifiFilter);
    }

    private BroadcastReceiver mUserInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onUserinfoChanged();
        }
    };

    public void onUserinfoChanged() {
        UserDefaults.defaults().reload();
        if (UserDefaults.defaults().getUserInfo() == null) {
            disableShakeListener();
        } else {
            loadKeyData();
        }
    }

    private boolean isSelectColse = false;
    private OnTaskCallback mBrushCardCallback = new OnTaskCallback() {
        @Override
        public void onSuccess() {
            mCurrentTask = null;
            if (!isSelectColse) {
                startVibrate();
                showToast("开门成功");
                SystemClock.sleep(500);
                startVibrate();
            }

        }

        @Override
        public void onError(final Throwable ex) {
            mCurrentTask = null;
            showToast(ex.getMessage());
        }

        @Override
        public void onShowSelect(String[] str) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext())
                    .setTitle("请选择")
                    .setItems(str, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mWifiAdmin.isWifiEnabled()) {
                                mCurrentTask.connectWIFI(which);
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isSelectColse = true;
                            mCurrentTask.setSelectColse(isSelectColse);
                            mCurrentTask.onSuccess();
                            mCurrentTask = null;
                        }
                    });
            AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.show();

        }

        @Override
        public void onConnected() {

        }
    };

}
