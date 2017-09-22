package com.hongbang.ic.keycenter.tasks;

import android.content.Context;

import com.hongbang.ic.keycenter.SimplePacket;
import com.hongbang.ic.keycenter.exceptions.TaskRuntimeException;
import com.hongbang.ic.util.FileUtils;
import com.hongbang.ic.util.Logger;

import org.xutils.x;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 下载刷卡记录
 * <p>
 * Created by xionghf on 16/5/22.
 */
public class DownloadRecordTask extends AbstractTask {
    private final String mBrushRecordDir;
    private String mSavePath;
    private String mTempSavePath;

    private static final int COMMAND = 0x86;

    public DownloadRecordTask(Context context) {
        super(context);
        mBrushRecordDir = mSaveRootDir + File.separator + "brush_record";
        if (!FileUtils.isDirectory(mBrushRecordDir)) {
            File f = new File(mBrushRecordDir);
            if (f.mkdirs()) {
                Logger.error(TAG, "创建文件夹失败: " + mBrushRecordDir);
            }
        }
    }

    @Override
    protected boolean checkSSID(String ssid) {
        return ssid != null && ssid.matches("^" + mCommunityId + "-\\d{1,3}$")
                && !ssid.equals(mCommunityId + "-000");
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
        return "下载刷卡记录完成";
    }

    @Override
    public String getErrorMessage() {
        return "下载刷卡记录失败";
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
            mSavePath = mBrushRecordDir + File.separator + mConnectedSSID + ".bin";
            mTempSavePath = mBrushRecordDir + File.separator + mConnectedSSID + "_temp.bin";
            FileUtils.delete(mSavePath);
            FileUtils.delete(mTempSavePath);
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


                x.task().post(new Runnable() {
                    @Override
                    public void run() {
                        File ff = new File(mSavePath);
                        int total = (int) ((ff.length() + 224 - 1) / 224);
                        mInnerTracker.onSendData("total =======" + total);
                        mInnerTracker.onSendData("文件大小 =======" + ff.length());
                    }
                });

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
