package com.hongbang.ic;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.hongbang.ic.api.CustomHttpManager;
import com.hongbang.ic.common.DefaultRequestTracker;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.service.BluetoothICService;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import org.xutils.http.request.UriRequestFactory;
import org.xutils.x;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * 自定义Application
 * <p/>
 * Created by xionghf on 16/3/23.
 */
public class HBICApplication extends MultiDexApplication {


    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        mContext = getApplicationContext();

        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
        CustomHttpManager.registerInstance();
        UriRequestFactory.registerDefaultTrackerClass(DefaultRequestTracker.class);

        UserDefaults.create(this);

        x.task().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 可以延后启动的任务
//                Bugly.init(getApplicationContext(), "900060262", x.isDebug());
                initBugly(x.isDebug());
            }
        }, 1000);
    }




    private void initBugly(boolean isDebug) {
        // 获取当前包名
        String packageName = mContext.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(mContext);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        strategy.setAppChannel("hongbang");  //设置渠道
        strategy.setAppVersion(getVersionName());      //App的版本
        strategy.setAppPackageName(packageName);  //App的包名 53b245660e  454a2d1b-6db2-49db-b6d0-133e643cf510
        // 初始化Bugly
        Bugly.init(mContext, "900060262", isDebug, strategy);
    }


    private  String getVersionName() {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = mContext.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }


    private  String getProcessName(int pid) {
        BufferedReader reader;
        reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public  String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
