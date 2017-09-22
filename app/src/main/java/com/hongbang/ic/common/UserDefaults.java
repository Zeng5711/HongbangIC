package com.hongbang.ic.common;

import android.content.Context;

import com.google.gson.Gson;
import com.hongbang.ic.model.MainData;
import com.hongbang.ic.model.UserInfo;
import com.hongbang.ic.util.AESUtils;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.SPUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * 用户数据
 * <p/>
 * Created by xionghf on 16/4/4.
 */
public class UserDefaults extends Observable {

    private Map<String, Object> mGlobalData = new HashMap<>();

    private Context mContext;

    private UserInfo mUserInfo = null;

    private MainData mMainData = null;

    private String mToken = null;

    private static UserDefaults instance;

    private UserDefaults(Context context) {
        mContext = context;
        String value = (String) SPUtils.get(mContext, SPUtils.KEY_USER_INFO, "");
        if (value != null) {
            try {
                mUserInfo = new Gson().fromJson(value, UserInfo.class);
            } catch (Exception e) {
                mUserInfo = null;
            }

        }

        String key = AppUtils.getDeviceId(mContext).substring(0, 16);
        String encrypted = (String) SPUtils.get(mContext, SPUtils.KEY_TOKEN, "");
        mToken = AESUtils.Decrypt(encrypted, key);
        if (mToken == null) {
            clear();
        }
    }

    public static void create(Context context) {
        if (instance == null) {
            synchronized (UserDefaults.class) {
                if (instance == null) {
                    instance = new UserDefaults(context);
                }
            }
        }
    }

    public static UserDefaults defaults() {
        if (instance == null) {
            throw new RuntimeException("Please call UserDefaults.create() before");
        } else {
            return instance;
        }
    }

    public void reload() {
        String value = (String) SPUtils.get(mContext, SPUtils.KEY_USER_INFO, "");
        if (value != null) {
            try {
                mUserInfo = new Gson().fromJson(value, UserInfo.class);
            } catch (Exception e) {
                mUserInfo = null;
            }

        }

        String key = AppUtils.getDeviceId(mContext).substring(0, 16);
        String encrypted = (String) SPUtils.get(mContext, SPUtils.KEY_TOKEN, "");
        mToken = AESUtils.Decrypt(encrypted, key);
        if (mToken == null) {
            clear();
        }
    }

    public void clear() {
        this.setUserInfo(null);
        this.setMainData(null);
    }

    public void setToken(String token) {
        mToken = token;
        if (token == null) {
            SPUtils.remove(mContext, SPUtils.KEY_TOKEN);
            clear();
        } else {
            String key = AppUtils.getDeviceId(mContext).substring(0, 16);
            String encrypted = AESUtils.Encrypt(token, key);
            SPUtils.put(mContext, SPUtils.KEY_TOKEN, encrypted);
        }
    }

    public String getToken() {
        return mToken;
    }

    public boolean isPropertyUser() {
        return mUserInfo != null && "物业".equals(mUserInfo.role);
    }

    public UserInfo getUserInfo() {
        if (mUserInfo != null) {
            return mUserInfo;
        }
        return null;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
        if (userInfo == null) {
            SPUtils.remove(mContext, SPUtils.KEY_USER_INFO);
        } else {
            String json = new Gson().toJson(userInfo);
            SPUtils.put(mContext, SPUtils.KEY_USER_INFO, json);
            setChanged();
            notifyObservers();
        }
    }

    public void saveUserInfo() {
        if (mUserInfo == null) {
            SPUtils.remove(mContext, SPUtils.KEY_USER_INFO);
        } else {
            String json = new Gson().toJson(mUserInfo);
            SPUtils.put(mContext, SPUtils.KEY_USER_INFO, json);
        }
    }

    public MainData getMainData() {
        if (mMainData == null) {
            String json = (String) SPUtils.get(mContext, SPUtils.KEY_COMMUNITY_DATA, "");
            if (json != null && json.length() > 0) {
                try {
                    try {
                        mMainData = new Gson().fromJson(json, MainData.class);
                    } catch (Exception e) {
                        mMainData = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return mMainData;
    }

    public void setMainData(MainData mainData) {
        mMainData = mainData;
        if (mainData == null) {
            SPUtils.remove(mContext, SPUtils.KEY_COMMUNITY_DATA);
        } else {
            String json = new Gson().toJson(mainData);
            SPUtils.put(mContext, SPUtils.KEY_COMMUNITY_DATA, json);
            setChanged();
            notifyObservers(mainData);
        }
    }


}
