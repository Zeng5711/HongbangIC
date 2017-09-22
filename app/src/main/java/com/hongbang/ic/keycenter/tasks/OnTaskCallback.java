package com.hongbang.ic.keycenter.tasks;

/**
 * 钥匙相关任务结果回调
 * <p>
 * Created by xionghf on 16/5/11.
 */
public interface OnTaskCallback {

    void onSuccess();

    void onConnected();

    void onError(Throwable ex);

    void onShowSelect(String str[]);

}
