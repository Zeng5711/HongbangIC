package com.hongbang.ic.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.os.Environment;
import android.util.Log;

import org.xutils.x;

public class Logger {

    private static final String TAG = "HongbangIC";

    private static final String LOG_DIR = Environment
            .getExternalStorageDirectory().getPath() + "/HongbangIC/";

    private static final String LOG_PATH = LOG_DIR + "log.txt";

    private static String appendMsg(Object... msgs) {
        StringBuilder msg = new StringBuilder();
        for (Object m : msgs) {
            if (m != null) {
                if (msg.length() > 0) {
                    msg.append(" ");
                }
                msg.append(m.toString());
            }
        }
        return msg.toString();
    }

    public static void info(String tag, Object... msgs) {
        if (!x.isDebug()) {
            return;
        }
        if (msgs == null || msgs.length == 0) {
            return;
        }

        String msg = appendMsg(msgs);

        Log.i(TAG + " " + tag, msg);

        writeLog("[INFO] " + tag.substring(tag.lastIndexOf(".") + 1) + ": "
                + msg);
    }

    public static void debug(String tag, Object... msgs) {
        if (!x.isDebug()) {
            return;
        }
        if (msgs == null || msgs.length == 0) {
            return;
        }

        String msg = appendMsg(msgs);

        Log.d(TAG + " " + tag, msg);

        writeLog("[DEBUG] " + tag.substring(tag.lastIndexOf(".") + 1) + ": "
                + msg);
    }

    public static void warn(String tag, Object... msgs) {
        if (msgs == null || msgs.length == 0) {
            return;
        }

        String msg = appendMsg(msgs);

        Log.w(TAG + " " + tag, msg);

        writeLog("[WARN] " + tag.substring(tag.lastIndexOf(".") + 1) + ": "
                + msg);
    }

    public static void error(String tag, Object... msgs) {
        if (msgs == null || msgs.length == 0) {
            return;
        }

        String msg = appendMsg(msgs);

        Log.e(TAG + " " + tag, msg);

        writeLog("[ERROR] " + tag.substring(tag.lastIndexOf(".") + 1) + ": "
                + msg);
    }

    public static void exception(String tag, Throwable e) {
        if (e == null) {
            return;
        }
        e.printStackTrace();
        writeLog("[ERROR] " + tag + ":");
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists() || !dir.isDirectory()) {
                try {
                    if (!dir.mkdirs()) {
                        System.out.println("创建路径失败");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            File logFile = new File(LOG_PATH);
            if (!logFile.exists() || !logFile.isFile()) {
                try {
                    if (!logFile.createNewFile()) {
                        System.out.println("创建路径失败");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            FileWriter writer = new FileWriter(LOG_PATH, true);
            PrintWriter pw = new PrintWriter(writer);
            pw.write("\r\n");
            e.printStackTrace(pw);
            pw.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private static void writeLog(String content) {
        try {
            StringBuilder log = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault());
            String currentDate = sdf.format(new Date());

            log.append(currentDate);
            log.append("  ");
            log.append(content);
            writeToFile(log.toString(), LOG_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected synchronized static void writeToFile(String log, String path) {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists() || !dir.isDirectory()) {
                try {
                    if (!dir.mkdirs()) {
                        System.out.println("创建路径失败");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            File logFile = new File(LOG_PATH);
            if (!logFile.exists() || !logFile.isFile()) {
                try {
                    if (!logFile.createNewFile()) {
                        System.out.println("创建路径失败");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            FileWriter writer = new FileWriter(path, true);
            writer.write(log.trim() + "\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
