package com.hongbang.ic.keycenter.tasks;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.hongbang.ic.activity.SettingsActivity;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.keycenter.AbstractPacket;
import com.hongbang.ic.keycenter.SimplePacket;
import com.hongbang.ic.keycenter.WifiAdmin;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.keycenter.exceptions.DeviceNotFoundException;
import com.hongbang.ic.keycenter.exceptions.NoResponseException;
import com.hongbang.ic.keycenter.exceptions.TaskRuntimeException;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.FileUtils;
import com.hongbang.ic.util.Logger;
import com.hongbang.ic.util.NotificationUtils;
import com.hongbang.ic.util.SPUtils;
import com.hongbang.ic.util.StringUtils;

import org.xutils.x;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.hongbang.ic.util.SPUtils.KEY_OPEN_DOOR_TIME;

/**
 * Task抽象类
 * <p/>
 * Created by xionghf on 16/5/11.
 */
public abstract class AbstractTask implements OnTaskCallback {

    protected final String TAG = getClass().getName();

    public static final int TASK_STATE_WAITING = 0;

    public static final int TASK_STATE_CONNECTING = 1;

    public static final int TASK_STATE_CONNECTED = 2;

    public static final int TASK_STATE_WORKING = 3;

    public static final int TASK_STATE_FINISHED = 4;

    public static final int TASK_STATE_ERROR = 5;

    public static final int TASK_STATE_DESTROY = 6;

    public static final int MAX_RETRY_COUNT = 5;

    public static final String WIFI_PASSWORD = "KaDaIc_2016";

    private String TARGET_IP = "192.168.100.254";

    private static final int TARGET_PORT = 5000;

    protected final String mSaveRootDir;

    protected final String mCommunityId;

    private int mState = TASK_STATE_WAITING;

    private OnTaskCallback mOnTaskCallback;

    protected WifiAdmin mWifiAdmin;

    protected String mConnectedSSID;

    protected ArrayBlockingQueue<String> mWifiStateQueue;

    protected Context mContext;

    private long mTaskTimeout = 6000;

    private static final String HISTORY_DIR = Environment
            .getExternalStorageDirectory().getPath() + "/HongbangIC/history";

    private final File mHistoryFile;

    protected boolean mRetryFlag = false;

    List<ScanResult> newList = new ArrayList<ScanResult>();

    protected boolean isSelectColse;

    public void setSelectColse(boolean isSelectColse) {
        this.isSelectColse = isSelectColse;
    }

    public AbstractTask(Context context) {
        mContext = context;
        mWifiAdmin = new WifiAdmin(context);
//        mSaveRootDir = context.getDir("manager", Context.MODE_PRIVATE).getPath()
//                + File.separator + UserDefaults.defaults().getUserInfo().communityId;
        mSaveRootDir = Environment.getExternalStorageDirectory().getPath() + "/HongbangIC" +
                File.separator + UserDefaults.defaults().getUserInfo().communityId;
        if (!FileUtils.isDirectory(mSaveRootDir)) {
            File f = new File(mSaveRootDir);
            if (f.mkdirs()) {
                Logger.error(TAG, "创建文件夹失败: " + mSaveRootDir);
            }
        }
        if (UserDefaults.defaults().getUserInfo() != null && UserDefaults.defaults().getUserInfo().communityId != null) {
            mCommunityId = String.format(Locale.getDefault(), "%05d",
                    Integer.parseInt(UserDefaults.defaults().getUserInfo().communityId));
        } else {
            mCommunityId = null;
        }

        String path = HISTORY_DIR + File.separator + getClass().getSimpleName().replace("Task", "") + StringUtils.formatDate(System.currentTimeMillis(),
                "_MM-dd_HH-mm-ss-sss") + ".txt";
        File dir = new File(HISTORY_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        mHistoryFile = new File(path);
        if (!mHistoryFile.exists()) {
            try {
                mHistoryFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void writeLog(String log) {
        try {
            FileWriter fw = new FileWriter(mHistoryFile, true);
            fw.write(log + "\r\n\r\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTargetIp(String targetIP) {
        if (StringUtils.isInet4Address(targetIP)) {
            TARGET_IP = targetIP;
        }
    }

    public void setWifiStateQueue(ArrayBlockingQueue<String> queue) {
        mWifiStateQueue = queue;
    }

    protected void setState(int state) {
        mState = state;
        mInnerTracker.onStateChanged(state);
    }

    public void start() {
        if (hasStarted()) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                setState(TASK_STATE_WORKING);
                try {
                    startTask();
                } catch (Exception e) {
                    e.printStackTrace();
                    AbstractTask.this.onError(new Exception("发生异常"));
                    mInnerTracker.onLog(e.getMessage());
                }
            }
        }).start();
    }

    protected void startTask() {
        if (mCommunityId == null) {
            onError(new Exception("发生异常"));
        }

//        setAutConn(true);

        newList.clear();
        WifiInfo wifiInfo = mWifiAdmin.getWifiInfo();
        boolean connected = false;
        String ssid = wifiInfo.getSSID();
        if (wifiInfo != null) {
            if (ssid != null && ssid.length() >= 9) {
                ssid = ssid.substring(1, ssid.length() - 1);
                connected = checkSSID(ssid);
                if (connected) {
                    mConnectedSSID = ssid;
                }
            }
        }

        if(connected){
            AbstractTask.this.onConnected();
            new ReceiverThread().start();
        }else {

            mWifiAdmin.startScan();
            List<ScanResult> list = mWifiAdmin.getWifiList();
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

                int oLevel = WifiManager.calculateSignalLevel(newList.get(0).level, 100);
                int tLevel = WifiManager.calculateSignalLevel(newList.get(1).level, 100);
                if (oLevel == tLevel) {
                    String wifi[] = new String[newList.size()];
                    for (int i = 0; i < newList.size(); i++) {
                        wifi[i] = newList.get(i).SSID;
                    }
                    if (null != wifi && wifi.length > 1) {
                        this.onShowSelect(wifi);
                    } else {
                        if (connected) {
                            startReceiverThread(mConnectedSSID);
                        } else {
                            connectWIFI(newList.get(0));
                        }
                    }
                } else if (oLevel - tLevel < 25) {
                    String wifi[] = new String[newList.size()];
                    for (int i = 0; i < newList.size(); i++) {
                        wifi[i] = newList.get(i).SSID;
                    }
                    if (null != wifi && wifi.length > 1) {
                        this.onShowSelect(wifi);
                    } else {
                        if (connected) {
                            startReceiverThread(mConnectedSSID);
                        } else {
                            connectWIFI(newList.get(0));
                        }
                    }
                } else {
                    if (connected) {
                        startReceiverThread(mConnectedSSID);
                    } else {
                        connectWIFI(newList.get(0));
                    }
                }
            } else if (null != newList && newList.size() == 1) {
                if (connected) {
                    startReceiverThread(mConnectedSSID);
                } else {
                    connectWIFI(newList.get(0));
                }
            } else {
                this.onError(new DeviceNotFoundException("附近没有可用设备"));
            }
        }
    }

    private void startReceiverThread(String ssid) {
        AbstractTask.this.onConnected();
        if (!TextUtils.isEmpty(ssid)) {
            mConnectedSSID = ssid;
        }
        mInnerTracker.onConnected(mConnectedSSID);
        new ReceiverThread().start();
    }

    public void connectWIFI(final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ScanResult scanResult = newList.get(position);
                WifiInfo wifiInfo = mWifiAdmin.getWifiInfo();
                if (wifiInfo != null) {
                    String ssid = wifiInfo.getSSID();
                    if (ssid != null && ssid.length() >= 9) {
                        ssid = ssid.substring(1, ssid.length() - 1);
                        if (ssid.equals(scanResult.SSID)) {
                            startReceiverThread(scanResult.SSID);
                        } else {
                            connectWIFI(scanResult);
                        }
                    } else {
                        connectWIFI(scanResult);
                    }
                } else {
                    connectWIFI(scanResult);
                }
            }
        }).start();

    }


    private void connectWIFI(ScanResult wifi) {
//        mWifiAdmin.disconnectWifi(mConnectedSSID);
        if (mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(wifi.SSID, WIFI_PASSWORD, 3))) {

            if (mWifiStateQueue != null) {

                AbstractTask.this.onConnected();
                String state = "";
                try {
                    mWifiStateQueue.clear();
                    state = mWifiStateQueue.poll(4, TimeUnit.SECONDS);
                    if ("connected".equals(state)) {
                        new ReceiverThread().start();
                        mConnectedSSID = wifi.SSID;
                        mInnerTracker.onConnected(mConnectedSSID);
                        if (mConnectedSSID.endsWith("-000")) {
                            mTaskTimeout = 30000;
                            NotificationUtils.sendNotification(mContext,
                                    "已连接到发卡器", "设备编号：" + mConnectedSSID, true);
                        } else {
                            NotificationUtils.sendNotification(mContext,
                                    "已连接到刷卡器", "设备编号：" + mConnectedSSID, true);
                        }


                    } else {
                        this.onError(new Exception("连接刷卡器失败，请重新连接！"));
                    }
                } catch (Exception e) {//Interrupted
                    e.printStackTrace();
                    this.onError(new Exception("连接刷卡器失败，请重新连接！"));
                }
            }
        } else {
            this.onError(new Exception("设备" + wifi.SSID + "不可用"));
        }
//        break;
    }

    private Timer mTimer;

    protected void sendTimeoutHandler() {
        if (mTimer != null) {
            try {
                mTimer.cancel();
                mTimer.purge();
                mTimer = null;
            } catch (Exception e) {
            }
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mTimeoutRunnable.run();
            }
        }, mTaskTimeout);
    }

    protected void sendTimeoutHandler(long timeout) {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mTimeoutRunnable.run();
            }
        }, timeout);
    }

    protected abstract boolean checkSSID(String ssid);

    Socket mSocket = null;

    private class   ReceiverThread extends Thread {
        @Override
        public void run() {
            try {
                setState(TASK_STATE_CONNECTING);
                mInnerTracker.onLog(String.format(Locale.getDefault(),
                        "准备连接%s:%d", TARGET_IP, TARGET_PORT));
                for (int i = 0; i < MAX_RETRY_COUNT; i++) {
                    try {
                        mSocket = new Socket();
                        mSocket.connect(new InetSocketAddress(InetAddress.getByName(TARGET_IP),
                                TARGET_PORT), 2000);
                        mSocket.setSoTimeout(1000*10);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (i == MAX_RETRY_COUNT - 1) {
                            throw e;
                        } else {
                            mInnerTracker.onLog(String.format(Locale.getDefault(),
                                    "连接%s:%d失败，第%d次重试", TARGET_IP, TARGET_PORT, i + 1));
                            SystemClock.sleep(100);
                        }
                    }
                }

                mInnerTracker.onLog(String.format(Locale.getDefault(),
                        "已成功连接到%s:%d", TARGET_IP, TARGET_PORT));

                sendTimeoutHandler();

                byte[] buffer = new byte[512];
                int length;

                DataInputStream input = new DataInputStream(new BufferedInputStream(mSocket.getInputStream(), 5 * 1024));

                AbstractPacket.mac = StringUtils.hex2Bytes(UserDefaults.defaults().getUserInfo().mobile + "0");
//                AbstractPacket.mac = StringUtils.hex2Bytes("68 3E 34 11 4A 80".replace(" ", ""));
                String garbageData = null;
                while (!mSocket.isClosed()) {
                    int oneByte;
                    if ((oneByte = (input.read() & 0xff)) == 0x55) {
                        if (garbageData != null) {
                            mInnerTracker.onLog("判断为垃圾数据:\n" + garbageData);
                            garbageData = null;
                        }

                        sendTimeoutHandler();
                        buffer[0] = 0x55;
                        buffer[1] = input.readByte();
                        buffer[2] = input.readByte();
                        length = (buffer[2] & 0xff) + 4;
                        int readLen = 3;
                        while (!mSocket.isClosed()) {
                            if (readLen < length) {
                                int hasRead = input.read(buffer, readLen, length - readLen);
                                if (hasRead <= 0) {
                                    continue;
                                }
                                readLen += hasRead;
                            } else {
                                break;
                            }
                        }

                        boolean split = isSplitPacket(buffer[1]);
                        int packetLen = (buffer[2] & 0xff) + 4;

                        if (length < packetLen || packetLen < (split ? 11 : 6)) {
                            if (x.isDebug()) {
                                String log = StringUtils.bytes2Hex2(buffer, 0, length);
                                mInnerTracker.onLog("报文长度不匹配(忽略该报文):\n" + log);
                            }
                            continue;
                        }

                        int checksum = 0;
                        for (int i = 0; i < packetLen - 1; i++) {
                            checksum += buffer[i] & 0xff;
                            checksum &= 0xff;
                        }
                        if ((checksum ^ (buffer[packetLen - 1] & 0xff)) != 0xff) {
                            if (x.isDebug()) {
                                String log = StringUtils.bytes2Hex2(buffer, 0, packetLen);
                                String cs = String.format("%02X", (checksum ^ 0xff) & 0xff);
                                mInnerTracker.onLog("报文校验和不正确(忽略该报文):" + cs + "\n" + log);
                            }
                            continue;
                        }

                        byte[] mac = new byte[6];
                        System.arraycopy(buffer, 3, mac, 0, 6);

                        if ((buffer[1] & 0xff) != 0x91 && !isMacEqual(mac, AbstractPacket.mac)) {
                            if (x.isDebug()) {
                                String log = StringUtils.bytes2Hex2(buffer, 0, packetLen);
                                mInnerTracker.onLog("手机号不匹配(忽略该报文):\n" + log);
                            }
                            continue;
                        }

                        x.task().removeCallbacks(mTimeoutRunnable);
                        if (x.isDebug()) {
                            String log = StringUtils.bytes2Hex2(buffer, 0, packetLen);
                            Logger.debug("KeyCenter", "receive: " + log);
                            mInnerTracker.onReceiveData(log);
                        }

                        int dataLen = (buffer[2] & 0xff) - (split ? 10 : 6);

                        byte[] data = new byte[dataLen];
                        System.arraycopy(buffer, split ? 13 : 9, data, 0, dataLen);

                        if ((buffer[1] & 0xff) == 0x91) {
                            if (doRegister(data)) {
                                sendTimeoutHandler();
                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        onRegisterSuccess();
                                    }
                                }, 15);
                            } else {
                                onError(new Exception("通信注册失败"));
                            }
                        } else {
                            sendTimeoutHandler();
                            if (split) {
                                int total = (buffer[9] & 0xff) + (buffer[10] & 0xff * 256);
                                int current = (buffer[11] & 0xff) + (buffer[12] & 0xff * 256);
                                dispatchCommand(buffer[1], total, current, data);
                            } else {
                                dispatchCommand(buffer[1], data);
                            }
                        }
                    } else {
                        if (garbageData == null) {
                            garbageData = String.format("%02X", oneByte);
                        } else {
                            garbageData += String.format(" %02X", oneByte);
                        }
                    }
                    SystemClock.sleep(15);
                }
                if (isWorking()) {
                    onError(new Exception("与设备的连接中断"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mCount = 0;
                onError(new Exception("与设备的连接发生异常"));
            }
        }
    }

    private boolean doRegister(byte[] data) {
        if (data.length < 4) {
            return false;
        }
        int input = (data[0] & 0xff) << 24 |
                (data[1] & 0xff) << 16 |
                (data[2] & 0xff) << 8 |
                (data[3] & 0xff);

        input ^= 0x89abcdef;
        input >>= 4;
        input &= 0x0fffffff;
        input ^= 0x23456789;

        SimplePacket p = new SimplePacket();
        p.cmd = (byte) 0x91;
        p.data = new byte[5];
        p.data[0] = (byte) (input >> 24 & 0xff);
        p.data[1] = (byte) (input >> 16 & 0xff);
        p.data[2] = (byte) (input >> 8 & 0xff);
        p.data[3] = (byte) (input & 0xff);
        p.data[4] = getRegisterType();

        return p.send(mSocket, mInnerTracker);
    }

    private boolean isCardIssuer() {
        return mConnectedSSID.endsWith("000");
    }

    public final void destroy() {
        setState(TASK_STATE_DESTROY);
        if (isWorking()) {
            onError(new Exception("强制停止"));
        } else {
            onDestroy();
        }
    }

    protected abstract byte getRegisterType();

    protected abstract void onRegisterSuccess();

    public abstract String getSuccessMessage();

    public abstract String getErrorMessage();

    protected abstract boolean isSplitPacket(byte command);

    protected abstract void dispatchCommand(byte command, byte[] data);

    protected abstract void dispatchCommand(byte command, int total, int current, byte[] data);

    protected abstract void onDestroy();

    protected abstract boolean retry();

    public void setOnTaskCallback(OnTaskCallback callback) {
        this.mOnTaskCallback = callback;
    }

    public boolean hasStarted() {
        return mState != TASK_STATE_WAITING;
    }

    public boolean isWorking() {
        return mState == TASK_STATE_WORKING || mState == TASK_STATE_CONNECTED
                || mState == TASK_STATE_CONNECTING;
    }

    @Override
    public void onConnected() {
        if (mOnTaskCallback != null) {
            x.task().post(new Runnable() {
                @Override
                public void run() {
                    mOnTaskCallback.onConnected();
                }
            });
        }
    }

    @Override
    public void onShowSelect(final String str[]) {
        if (mOnTaskCallback != null) {
            x.task().post(new Runnable() {
                @Override
                public void run() {
                    mOnTaskCallback.onShowSelect(str);
                }
            });
        }
    }

    @Override
    public synchronized void onSuccess() {
        if (!isWorking()) {
            return;
        }
        x.task().removeCallbacks(mTimeoutRunnable);
        SystemClock.sleep(500);
        setState(TASK_STATE_FINISHED);
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOnTaskCallback != null) {
            x.task().post(new Runnable() {
                @Override
                public void run() {
                    mOnTaskCallback.onSuccess();
                }
            });
        }
        mWifiAdmin.disconnectWifi(mConnectedSSID);
        onDestroy();
        Logger.debug(TAG, "操作成功");
        mCount = 0;
    }

    private int mCount = 0;

    @Override
    public synchronized void onError(final Throwable ex) {
        if (!isWorking()) {
            return;
        }
        if(mCount == 1) {
            x.task().removeCallbacks(mTimeoutRunnable);
            ex.printStackTrace();
            if (mState != TASK_STATE_DESTROY) {
                setState(TASK_STATE_ERROR);
            }
            if (mSocket != null && !mSocket.isClosed()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (retry()) {
                mRetryFlag = false;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startTask();
                    }
                }, AppConstants.BRUSH_CARD_RETRY_INTERVAL);
                return;
            }
            if (mOnTaskCallback != null) {
                x.task().post(new Runnable() {
                    @Override
                    public void run() {
                        mOnTaskCallback.onError(ex);
                    }
                });
            }
            mInnerTracker.onError(ex);
            onDestroy();
            Logger.exception(TAG, ex);
        }else{
            mCount ++;
            SystemClock.sleep(200);
            startTask();
        }
    }


    protected Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            onError(new NoResponseException("过长时间没有数据交互"));
        }
    };

    private OnTaskTracker mTaskTracker;

    public void setTracker(OnTaskTracker tracker) {
        mTaskTracker = tracker;
    }

    private String[] states = {"任务开始", "连接中...", "连接成功",
            "任务开始", "任务结束", "任务出错", "任务中止"};

    protected OnTaskTracker mInnerTracker = new OnTaskTracker() {
        @Override
        public void onStateChanged(final int state) {
            if (x.isDebug()) {
                writeLog(">>" + states[state]);
                if (mTaskTracker != null) {
                    x.task().post(new Runnable() {
                        @Override
                        public void run() {
                            mTaskTracker.onStateChanged(state);
                        }
                    });
                }
            }
        }

        @Override
        public void onConnected(final String ssid) {
            if (x.isDebug()) {
                writeLog("连接到Wifi: " + ssid);
                if (mTaskTracker != null) {
                    x.task().post(new Runnable() {
                        @Override
                        public void run() {
                            mTaskTracker.onConnected(ssid);
                        }
                    });
                }
            }
        }

        @Override
        public void onReceiveData(final String hex) {
            if (x.isDebug()) {
                writeLog(">>收到数据:\n" + hex);
                if (mTaskTracker != null) {
                    x.task().post(new Runnable() {
                        @Override
                        public void run() {
                            mTaskTracker.onReceiveData(hex);
                        }
                    });
                }
            }
        }

        @Override
        public void onSendData(final String hex) {
            sendTimeoutHandler(10000);
            if (x.isDebug()) {
                writeLog(">>发送数据:\n" + hex);
                if (mTaskTracker != null) {
                    x.task().post(new Runnable() {
                        @Override
                        public void run() {
                            mTaskTracker.onSendData(hex);
                        }
                    });
                }
            }
        }

        @Override
        public void onError(final Throwable ex) {
            if (x.isDebug()) {
                if (ex != null) {
                    writeLog(">>异常信息:\n" + ex.getMessage());
                }
                if (mTaskTracker != null) {
                    x.task().post(new Runnable() {
                        @Override
                        public void run() {
                            mTaskTracker.onError(ex);
                        }
                    });
                }
            }
        }

        @Override
        public void onLog(final String log) {
            if (x.isDebug()) {
                writeLog(log);
                if (mTaskTracker != null) {
                    x.task().post(new Runnable() {
                        @Override
                        public void run() {
                            mTaskTracker.onLog(log);
                        }
                    });
                }
            }
        }
    };

    private boolean isMacEqual(byte[] arg0, byte[] arg1) {
        if (arg0 == arg1) {
            return true;
        } else if (arg0 == null || arg0.length < 6 || arg1 == null || arg1.length < 6) {
            return false;
        } else {
            for (int i = 0; i < 6; i++) {
                if (arg0[i] != arg1[i]) {
                    return false;
                }
            }
            return true;
        }
    }

}
