package com.hongbang.ic.keycenter.tasks;

import android.content.Context;

import com.hongbang.ic.keycenter.SimplePacket;
import com.hongbang.ic.keycenter.exceptions.TaskRuntimeException;
import com.hongbang.ic.util.FileUtils;

import org.xutils.x;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 下载白名单
 * <p>
 * Created by xionghf on 16/5/22.
 */
public class DownloadWhiteListTask extends AbstractTask {
    private String mSavePath;
    private String mTempSavePath;

    private static final int COMMAND = 0x87;

    public DownloadWhiteListTask(Context context) {
        super(context);
        mSavePath = mSaveRootDir + File.separator + "white.bin";
        mTempSavePath = mSaveRootDir + File.separator + "white_temp.bin";
        FileUtils.delete(mSavePath);
        FileUtils.delete(mTempSavePath);
    }

    @Override
    protected boolean checkSSID(String ssid) {
        return ssid != null && ssid.equals(mCommunityId + "-000");
//        return ssid != null && ssid.matches("^" + mCommunityId + "-\\d{1,3}$")
//                && !ssid.equals(mCommunityId + "-000");
    }

    @Override
    protected byte getRegisterType() {
        return 0x51;
    }

    @Override
    protected void onRegisterSuccess() {
        SimplePacket p = new SimplePacket();
        p.cmd = (byte) COMMAND;
        if (!p.send(mSocket, mInnerTracker)) {
            onError(new TaskRuntimeException("发送黑名单下载请求失败"));
        }
    }

    @Override
    public String getSuccessMessage() {
        return "下载白名单完成";
    }

    @Override
    public String getErrorMessage() {
        return "下载白名单失败";
    }

    @Override
    protected boolean isSplitPacket(byte cmd) {
        return (cmd & 0xff) == COMMAND;
    }

    @Override
    protected void dispatchCommand(byte cmd, byte[] data) {
    }

    private BufferedOutputStream mOutputStream = null;
    private FileOutputStream mFileOutputStream = null;

    @Override
    protected void dispatchCommand(byte cmd, int total, int current, byte[] data) {
        if (mOutputStream == null) {
            File f = new File(mTempSavePath);
            try {
                if (!f.createNewFile()) {
                    onError(new TaskRuntimeException("创建文件失败"));
                }
            } catch (IOException e) {
                onError(e);
                return;
            }

            try {
                mFileOutputStream = new FileOutputStream(f, false);
            } catch (FileNotFoundException e) {
                onError(e);
                return;
            }
            mOutputStream = new BufferedOutputStream(mFileOutputStream);
        }

        try {
            mOutputStream.write(data);
            mOutputStream.flush();
        } catch (IOException e) {
            onError(e);
        }

        if (current == total) {
            if (new File(mTempSavePath).renameTo(new File(mSavePath))) {
                onSuccess();
            } else {
                onError(new TaskRuntimeException("文件保存失败"));
            }
        } else {
            sendTimeoutHandler(1500);
        }
    }

    @Override
    protected void onDestroy() {
        FileUtils.delete(mTempSavePath);
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        x.task().removeCallbacks(mTimeoutRunnable);
    }

    @Override
    protected boolean retry() {
        return false;
    }
}
