package com.hongbang.ic.keycenter.tasks;

/**
 * 钥匙相关任务测试用类
 * <p/>
 * Created by xionghf on 16/5/11.
 */
public interface OnTaskTracker {

    void onStateChanged(int state);

    void onConnected(String ssid);

    void onReceiveData(String hex);

    void onSendData(String hex);

    void onError(Throwable ex);

    void onLog(String log);

}
