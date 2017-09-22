package com.hongbang.ic.keycenter.tasks;

import android.content.Context;
import android.os.Environment;

import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.keycenter.KeyUtils;
import com.hongbang.ic.keycenter.SimplePacket;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.util.NotificationUtils;
import com.hongbang.ic.util.SPUtils;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.hongbang.ic.util.SPUtils.KEY_OPEN_DOOR_TIME;

/**
 * 刷卡
 * <p/>
 * Created by xionghf on 16/5/11.
 */
public class OpenDoorTask extends AbstractTask {

    private List<OneKeyInfo4DB> mAllKeyList = new ArrayList<>();

    public OpenDoorTask(Context context) {
        super(context);
    }

    private void loadKeyData() {
        mAllKeyList = KeyUtils.getAll();
    }

    private int mErrorCount = 0;

    @Override
    protected void startTask() {
        loadKeyData();
        mErrorCount = 0;
        if (mAllKeyList == null || mAllKeyList.size() == 0) {
            this.onError(new Exception("没有找到可用的钥匙包"));
            return;
        }
        super.startTask();
    }

    @Override
    protected boolean checkSSID(String ssid) {
        return ssid != null && ssid.matches("^" + mCommunityId + "-\\d{1,3}$")&& !ssid.equals(mCommunityId + "-000");
    }

    @Override
    protected boolean isSplitPacket(byte command) {
        return false;
    }

    protected void dispatchCommand(byte cmd, byte[] data) {
        int command = cmd & 0xff;
        if (command == 0x92) {
            mRetryFlag = true;
            sendAllData();
        } else if (command == 0x81) {
            mRetryFlag = true;
            sendKeyData();
        } else if (command == 0x82) {
            sendRollPackage(data);
        } else if (command == 0x83) {
            updateRollPackage(data);
        }
    }

    @Override
    protected void dispatchCommand(byte command, int total, int current, byte[] data) {
    }

    @Override
    protected void onDestroy() {

    }

    @Override
    protected boolean retry() {
        privateKey = null;
        temporaryKey = null;
        return mRetryFlag && mAllKeyList.size() > 0;
    }

    @Override
    protected byte getRegisterType() {
        return 0x01;
    }

    @Override
    protected void onRegisterSuccess() {
    }

    @Override
    public String getSuccessMessage() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    private OneKeyInfo4DB privateKey = null;
    private OneKeyInfo4DB temporaryKey = null;

    private void sendAllData() {
        if (mErrorCount >= 3) {
            onError(new Exception("开门失败"));
            return;
        }
        for (OneKeyInfo4DB keyInfo : mAllKeyList) {
            if (privateKey == null && keyInfo.type != AppConstants.KEY_TYPE_SHARED) {
                privateKey = keyInfo;
            }
            if (temporaryKey == null && keyInfo.type == AppConstants.KEY_TYPE_SHARED) {
                temporaryKey = keyInfo;
            }
            if (privateKey != null && temporaryKey != null) {
                break;
            }
        }

        if (privateKey == null && temporaryKey == null) {
            onError(new Exception("没有找到可用钥匙"));
            return;
        }
        if (privateKey != null) {
            mAllKeyList.remove(privateKey);
        }
        if (temporaryKey != null) {
            mAllKeyList.remove(privateKey);
        }

        String key = "";
        if (privateKey != null) {
            key += privateKey.defaultKey + privateKey.defaultRoll;
        }

        if (temporaryKey != null) {
            key += temporaryKey.defaultKey + temporaryKey.defaultRoll;
        }

        byte[] bytePhone = StringUtils.hex2Bytes(UserDefaults.defaults().getUserInfo().mobile + "0");
        byte[] byteKey = StringUtils.hex2Bytes(key);

        byte[] data = new byte[byteKey.length + bytePhone.length];
        System.arraycopy(bytePhone, 0, data, 0, bytePhone.length);
        System.arraycopy(byteKey, 0, data, 6, byteKey.length);

        SimplePacket packet = new SimplePacket();
        packet.cmd = (byte) (0x92 & 0xff);
        packet.data = data;
        if (!packet.send(mSocket, mInnerTracker)) {
            onError(new Exception("发送钥匙包失败"));
        } else {
            mErrorCount ++;
        }
    }

    private void sendKeyData() {
        if (mErrorCount >= 3) {
            onError(new Exception("开门失败"));
            return;
        }
        if (privateKey == null && temporaryKey == null) {
            if (mAllKeyList.size() == 0) {
                onError(new Exception("没有找到可用钥匙"));
                return;
            }
            OneKeyInfo4DB keyInfo = mAllKeyList.remove(0);
            if (keyInfo.type != AppConstants.KEY_TYPE_SHARED) {
                privateKey = keyInfo;
            } else {
                temporaryKey = keyInfo;
            }
        }

        String key;
        byte type = 0x01;
        if (privateKey != null) {
            key = privateKey.defaultKey + privateKey.defaultRoll;
        } else {
            type = 0x02;
            key = temporaryKey.defaultKey + temporaryKey.defaultRoll;
        }

        byte[] bytePhone = StringUtils.hex2Bytes(UserDefaults.defaults().getUserInfo().mobile + "0");
        byte[] byteKey = StringUtils.hex2Bytes(key);

        byte[] data = new byte[byteKey.length + bytePhone.length + 1];
        System.arraycopy(bytePhone, 0, data, 0, bytePhone.length);
        data[6] = type;
        System.arraycopy(byteKey, 0, data, 7, byteKey.length);

        SimplePacket packet = new SimplePacket();
        packet.cmd = (byte) (0x81 & 0xff);
        packet.data = data;
        if (!packet.send(mSocket, mInnerTracker)) {
            onError(new Exception("发送钥匙包失败"));
        } else {
            mErrorCount ++;
        }
    }

    private void sendRollPackage(byte[] data) {
        String roll;
        if (data.length < 1 || (data[0] & 0xff) == 0x90) {
            if (privateKey == null) {
                onError(new Exception("通信异常"));
                return;
            } else {
                roll = privateKey.defaultRoll;
            }
        } else {
            if (temporaryKey == null) {
                onError(new Exception("通信异常"));
                return;
            } else {
                roll = temporaryKey.defaultRoll;
            }
        }

        byte[] byteRoll = StringUtils.hex2Bytes(roll);

        SimplePacket packet = new SimplePacket();
        packet.cmd = (byte) (0x82 & 0xff);
        packet.data = byteRoll;
        if (!packet.send(mSocket, mInnerTracker)) {
            onError(new Exception("发送钥匙包失败"));
        }
    }

    private void updateRollPackage(byte[] data) {
        byte result = (byte) 0xa5;
        block:
        {
            if (data.length < 36) {
                break block;
            }
            boolean isPrivate = (data[0] & 0xff) == 0x90;
            String roll = StringUtils.bytes2Hex(data, 1, 35);
            if (isPrivate) {
                if (privateKey == null) {
                    break block;
                }
                privateKey.defaultRoll = roll;
                if (KeyUtils.update(privateKey)) {
                    result = (byte) 0x5a;
                }
            } else {
                if (temporaryKey == null) {
                    break block;
                }
                temporaryKey.defaultRoll = roll;
                if (KeyUtils.update(temporaryKey)) {
                    result = (byte) 0x5a;
                }
            }
        }

        SimplePacket p = new SimplePacket();
        p.cmd = (byte) 0x83;
        p.data = new byte[1];
        p.data[0] = result;
        if (p.send(mSocket, mInnerTracker)) {
            onSuccess();
        } else {
            onError(new Exception("写入滚码包失败"));
        }

    }
    

    @Override
    public synchronized void onSuccess() {

        if(!isSelectColse) {
            NotificationUtils.sendNotification(mContext,
                    "开门成功", "设备为" + mConnectedSSID);


            x.task().post(new Runnable() {
                @Override
                public void run() {
                    T.showShort(x.app(), "设备为" + mConnectedSSID);
                }
            });
//            mWifiAdmin.disconnectWifi(mConnectedSSID);
        }

        SPUtils.put(mContext, KEY_OPEN_DOOR_TIME, System.currentTimeMillis());
        super.onSuccess();

    }

}
