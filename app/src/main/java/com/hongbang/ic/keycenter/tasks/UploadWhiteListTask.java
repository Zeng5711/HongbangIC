package com.hongbang.ic.keycenter.tasks;

import android.content.Context;

import com.hongbang.ic.keycenter.SplitPacket;
import com.hongbang.ic.keycenter.exceptions.TaskRuntimeException;
import com.hongbang.ic.util.FileUtils;

import org.xutils.x;

import java.io.File;

/**
 * 上传白名单
 * <p/>
 * Created by xionghf on 16/5/22.
 */
public class UploadWhiteListTask extends AbstractTask {
    private String mSavePath;

    private static final int COMMAND = 0x84;

    private SplitPacket.Task mSendTask;

    public UploadWhiteListTask(Context context) {
        super(context);
        mSavePath = mSaveRootDir + File.separator + "white.bin";
    }

    @Override
    public void start() {
        if (hasStarted()) {
            return;
        }

        if (!FileUtils.isFile(mSavePath)) {
            setState(TASK_STATE_WORKING);
            mInnerTracker.onLog("本地没有未上传的白名单数据");
            onError(new Exception("本地没有未上传的白名单数据"));
        } else {
            super.start();
        }
    }

    @Override
    protected boolean checkSSID(String ssid) {
//        return ssid != null && ssid.matches("^" + mCommunityId + "-\\d{1,3}$")
//                && !ssid.equals(mCommunityId + "-000");
        return ssid != null && ssid.matches("^" + mCommunityId + "-\\d{1,3}$")
                && !ssid.equals(mCommunityId + "-000");
    }

    @Override
    protected byte getRegisterType() {
        return 0x51;
    }

    @Override
    protected void onRegisterSuccess() {
        mSendTask = new SplitPacket.Task()
                .socket(mSocket)
                .command(COMMAND)
                .file(new File(mSavePath))
                .tracker(mInnerTracker)
                .callback(new SplitPacket.ISendCallback() {
                    @Override
                    public void onSuccess() {
                        sendTimeoutHandler();
                    }

                    @Override
                    public void onError(Throwable ex) {
                        UploadWhiteListTask.this.onError(ex);
                    }

                    @Override
                    public void onSendData(int total, int current) {
                        sendTimeoutHandler();
                    }
                })
                .start();
    }

    @Override
    public String getSuccessMessage() {
        return "上传白名单完成";
    }

    @Override
    public String getErrorMessage() {
        return "上传白名单失败";
    }

    @Override
    protected boolean isSplitPacket(byte cmd) {
        return false;
    }

    @Override
    protected void dispatchCommand(byte cmd, byte[] data) {
        int command = cmd & 0xff;
        if (command == COMMAND) {
            x.task().removeCallbacks(mTimeoutRunnable);
            if (data.length > 0 && (data[0] & 0xff) == 0x5a) {
//                FileUtils.delete(mSavePath);
                onSuccess();
            } else {
                if (mSendTask != null) {
                    mSendTask.interrupt();
                }
                onError(new TaskRuntimeException("上传白名单失败"));
            }
        }
    }

    @Override
    protected void dispatchCommand(byte cmd, int total, int current, byte[] data) {

    }

    @Override
    protected void onDestroy() {
        x.task().removeCallbacks(mTimeoutRunnable);
    }

    @Override
    protected boolean retry() {
        return false;
    }
}
