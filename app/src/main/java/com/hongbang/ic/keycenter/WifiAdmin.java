package com.hongbang.ic.keycenter;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.SystemClock;
import android.util.Log;

import com.hongbang.ic.util.Logger;
import com.hongbang.ic.util.SPUtils;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class WifiAdmin {

    private static final String TAG = "[WifiAdmin]";
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList = null;
    private List<WifiConfiguration> mWifiConfiguration;
    private WifiLock mWifiLock;

    public WifiAdmin(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    /**
     * 打开wifi
     *
     * @return 系统是否同意打开wifi
     */
    public boolean openWifi() {
        if (!mWifiManager.isWifiEnabled()
                && mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
            Logger.info(TAG, "setWifiEnabled.....");
            return mWifiManager.setWifiEnabled(true);
        }
        return true;
    }

    public void closeWifi() {
        mWifiManager.setWifiEnabled(false);
    }

    public int getWifiState() {
        return mWifiManager.getWifiState();
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public boolean isWifiEnabling() {
        return mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
    }

    /**
     * 锁定wifiLock
     */
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    /**
     * 解锁wifiLock
     */
    public void releaseWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    public String getSSID(){
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
    }

    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    /**
     * 指定配置好的网络进行连接
     *
     * @param index 下标
     */
    public void connectConfiguration(int index) {
        if (index > mWifiConfiguration.size()) {
            return;
        }
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
    }

    /**
     * wifi扫描
     */
    public void startScan() {
        int tryCount = 0;
        do {
//            if (tryCount > 0) {
//            }
            mWifiManager.startScan();
            SystemClock.sleep(tryCount * 1000);
            try {
                mWifiList = mWifiManager.getScanResults();
            } catch (Exception e) {
                e.printStackTrace();
            }
            tryCount++;
        } while ((mWifiList == null || mWifiList.size() == 0) && tryCount < 4);
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }

    public List<ScanResult> getWifiList() {
        List<ScanResult> results = new ArrayList<>();
        if (mWifiList != null) {
            for (ScanResult sr : mWifiList) {
                if (WifiManager.calculateSignalLevel(sr.level, 100) >= 100 - (int) SPUtils.get(x.app(), SPUtils.KEY_WIFI_SENSITIVITY, 100)) {
                    results.add(sr);
                }
            }
        }
        return results;
    }

    /**
     * 查看
     *
     * @return 扫描结果
     */
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_");
            stringBuilder.append(Integer.valueOf(i + 1).toString());
            stringBuilder.append(Integer.valueOf(i + 1).toString());
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }

    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public DhcpInfo getDhcpInfo() {
        return mWifiManager.getDhcpInfo();
    }

    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    public WifiInfo getWifiInfo() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        return mWifiInfo;
    }

    /**
     * 添加一个网络配置并连接
     */
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID;
        if (wcg.networkId > 0) {
            wcgID = wcg.networkId;
        } else {
            wcgID = mWifiManager.addNetwork(wcg);
        }
        if (wcgID <= 0) {
            return false;
        }
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        System.out.println("addNetwork--" + wcgID);
        System.out.println("enableNetwork--" + b);
        return b;
    }

    public WifiManager getWifiManager(){
        return mWifiManager;
    }

    public void disableNetwork(String SSID){
        if (SSID == null || SSID.length() == 0) {
            return;
        }
        WifiConfiguration config;
        if ((config = isExsits(SSID)) != null) {
            mWifiManager.disableNetwork(config.networkId);
        }
    }

    public void disconnect(){
        mWifiManager.disconnect();
    }


    public void disconnectWifi(String SSID) {
        if (SSID == null || SSID.length() == 0) {
            return;
        }
        if (getWifiInfo().getSSID().matches("\\\"+SSID+\\\"")) {
            mWifiManager.disconnect();
        }
        WifiConfiguration config;
        if ((config = isExsits(SSID)) != null) {
            mWifiManager.disableNetwork(config.networkId);
        }

    }

    public WifiConfiguration createWifiInfo(String SSID, String password, int type) {
        Logger.info(TAG, "SSID:" + SSID + ",password:" + password);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.isExsits(SSID);

        if (tempConfig != null) {
            return tempConfig;
//            mWifiManager.removeNetwork(tempConfig.networkId);
        } else {
            Logger.info(TAG, "Is Exists is null.");
        }

        if (type == 1) {
            // WIFICIPHER_NOPASS
            Logger.info(TAG, "Type =1.");
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 2) {
            // WIFICIPHER_WEP
            Logger.info(TAG, "Type =2.");
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 3) {
            // WIFICIPHER_WPA
            Logger.info(TAG, "Type =3.");
            config.preSharedKey = "\"" + password + "\"";

            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 查看以前是否已经配置过该SSID
     *
     * @param SSID ssid
     * @return 配置信息
     */
    private WifiConfiguration isExsits(String SSID) { //
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs == null) {
            return null;
        }
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }
}