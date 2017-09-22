package com.hongbang.ic.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import org.xutils.x;

import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * 跟App相关的辅助类
 * <p>
 * Created by xionghf on 16/3/27.
 */
public class AppUtils {

    private AppUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");

    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context 上下文
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备唯一ID
     *
     * @param context 上下文
     * @return 设备唯一ID
     */
    public static String getDeviceId(Context context) {
        String mac, deviceId;
        mac = getWifiMacAddress();
        if (!StringUtils.isEmpty(mac)) {
            deviceId = StringUtils.MD5(mac);
            SPUtils.put(context, SPUtils.KEY_DEVICE_ID, deviceId);
        } else {
            deviceId = (String) SPUtils.get(context, SPUtils.KEY_DEVICE_ID, "");
            if (StringUtils.isEmpty(deviceId)) {
                mac = getMacFromDevice(context);
                if (StringUtils.isEmpty(mac)||mac.contains("02:00:00:00:00")) {
                    T.showShort(x.app(), "获取设备ID失败");
                    System.exit(0);
                    return null;
                } else {
                    deviceId = StringUtils.MD5(mac);
                    SPUtils.put(context, SPUtils.KEY_DEVICE_ID, deviceId);
                }
            }
        }
        return deviceId;
    }

    private static boolean tryOpenMAC(WifiManager manager) {
        boolean softOpenWifi = false;
        int state = manager.getWifiState();
        if (state != WifiManager.WIFI_STATE_ENABLED && state != WifiManager.WIFI_STATE_ENABLING) {
            manager.setWifiEnabled(true);
            softOpenWifi = true;
        }
        return softOpenWifi;
    }

    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    //尝试读取MAC地址
    private static String getMacFromDevice(Context context) {
        String mac;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mac = getWifiMacAddress();
        if (!StringUtils.isEmpty(mac)) {
            return mac;
        }

        //获取失败，尝试打开wifi获取
        tryOpenMAC(wifiManager);
        for (int index = 0; index < 10; index++) {
            //如果第一次没有成功，第二次做200毫秒的延迟。
            if (index != 0) {
                SystemClock.sleep(200);
            }
            mac = getWifiMacAddress();
            if (!StringUtils.isEmpty(mac)) {
                break;
            }
        }

        return mac;
    }

    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public static boolean shouldShowGuideView(Context context) {
        try {
            int newVersion = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
            int oldVersion = (int) SPUtils.get(context, SPUtils.KEY_LAST_VERSION, -1);
            if (oldVersion != newVersion) {
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void appFirstStarted(Context context) {
        try {
            int newVersion = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
            SPUtils.put(context, SPUtils.KEY_LAST_VERSION, newVersion);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = myAM.getRunningServices(40);
        if (services.size() <= 0) {
            return false;
        }
        for (int i = 0; i < services.size(); i++) {
            String name = services.get(i).service.getClassName();
            if (name != null && name.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String[] projection = {MediaStore.Images.Media.DATA};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}