package com.hongbang.ic.keycenter.tasks;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

import com.hongbang.ic.HBICApplication;
import com.hongbang.ic.keycenter.SplitPacket;
import com.hongbang.ic.keycenter.exceptions.TaskRuntimeException;
import com.hongbang.ic.util.FileUtils;
import com.hongbang.ic.util.Logger;
import com.hongbang.ic.util.T;

import org.xutils.common.util.FileUtil;
import org.xutils.x;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 上传刷卡记录
 * <p/>
 * Created by xionghf on 16/5/22.
 */
public class UploadRecordTask extends AbstractTask {
//    private final String mBrushRecordDir;
//    private final List<File> mRecordFiles;

    private final String mUploadPath;

    private static final int COMMAND = 0x96;

    private SplitPacket.Task mSendTask;

    public UploadRecordTask(Context context, String uploadPath) {
        super(context);
        mUploadPath = uploadPath;
//        mBrushRecordDir = mSaveRootDir + File.separator + "brush_record";
//
//        if (!FileUtils.isDirectory(mBrushRecordDir)) {
//            mRecordFiles = null;
//        } else {
//            mRecordFiles = new ArrayList<>();
//            File dir = new File(mBrushRecordDir);
//            String[] files = dir.list();
//            for (int i = 0; files != null && files.length > 0 && i < files.length; i++) {
//                File file = new File(mBrushRecordDir, files[i]);
//
//                if (file.isFile() && file.length() > 0
//                        && file.getName().matches("^" + mCommunityId + "-\\d{1,3}\\.bin$")) {
//                    mRecordFiles.add(file);
//                }
//            }
//        }
    }

    @Override
    public void start() {
        if (hasStarted()) {
            return;
        }

        if (!FileUtils.isFile(mUploadPath)) {
            setState(TASK_STATE_WORKING);
            mInnerTracker.onLog("本地没有未上传的刷卡记录");
            onError(new Exception("本地没有未上传的刷卡记录"));
        } else {
            super.start();
        }
    }

    @Override
    protected boolean checkSSID(String ssid) {
        return ssid != null && ssid.equals(mCommunityId + "-000");
    }

    @Override
    protected byte getRegisterType() {
        return 0x51;
    }

    @Override
    protected void onRegisterSuccess() {
        uploadRecord(0);
    }

    @Override
    public String getSuccessMessage() {
        return "上传刷卡记录完成";
    }

    @Override
    public String getErrorMessage() {
        return "上传刷卡记录失败";
    }

    private File uploadingFile;

    private void uploadRecord(long delayed) {
//        if (mRecordFiles != null && mRecordFiles.size() > 0) {
//            uploadingFile = mRecordFiles.remove(0);
        mSendTask = new SplitPacket.Task()
                .socket(mSocket)
                .command(COMMAND)
                .file(new File(mUploadPath))
                .callback(mSendCallback)
                .tracker(mInnerTracker)
                .startDelayed(delayed);
//        } else {
//            onSuccess();
//        }
    }

    private SplitPacket.ISendCallback mSendCallback = new SplitPacket.ISendCallback() {
        @Override
        public void onSuccess() {
            sendTimeoutHandler();
        }

        @Override
        public void onError(Throwable ex) {
            UploadRecordTask.this.onError(ex);
        }

        @Override
        public void onSendData(final int total, int current) {
            sendTimeoutHandler();
            x.task().post(new Runnable() {
                @Override
                public void run() {
                    mInnerTracker.onSendData("total =======" + total);
                    mInnerTracker.onSendData("文件大小 =======" + new File(mUploadPath).length());
                }
            });
        }
    };

    @Override
    protected boolean isSplitPacket(byte cmd) {
        return false;
    }

    @Override
    protected void dispatchCommand(byte cmd, byte[] data) {
        int command = cmd & 0xff;
        if (command == COMMAND) {
            sendTimeoutHandler();
            if (data.length > 0 && (data[0] & 0xff) == 0x5a) {
                if (uploadingFile != null) {
                    FileUtils.delete(uploadingFile.getPath());
                }
//                uploadRecord(5000);
                onSuccess();
            } else {
                if (mSendTask != null) {
                    mSendTask.interrupt();
                }
                onError(new TaskRuntimeException("上传刷卡记录失败"));
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
