package com.hongbang.ic.util;

import android.content.Context;
import android.content.SharedPreferences;

import net.grandcentrix.tray.TrayPreferences;
import net.grandcentrix.tray.core.SharedPreferencesImport;

import org.xutils.x;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class SPUtils {
    /**
     * 保存在手机里面的文件名
     */
    public static final String FILE_NAME = "user_defaults";

    public static final String KEY_USER_INFO = "user_info";

    public static final String KEY_TOKEN = "token";

    public static final String KEY_LAST_VERSION = "last_version";

    public static final String KEY_COMMUNITY_DATA = "community_data";

    public static final String KEY_DEVICE_ID = "device_id";

    public static final String KEY_SHAKE_ENABLED = "open_when_shake";

    public static final String KEY_AUTO_DISCONNECT = "auto_disconnect_wifi";

    public static final String KEY_WIFI_SENSITIVITY = "wifi_sensitivity";

    public static final String KEY_SCREEN_ON_ENABLED = "open_when_screen_on";

    public static final String KEY_OPEN_DOOR_TIME = "open_door_time";

    public static final String KEY_AUT_CONN = "aut_conn";

    private static AppPreferences mAppPreferences;

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void put(Context context, String key, Object object) {
        if (mAppPreferences == null) {
            mAppPreferences = new AppPreferences();
        }

        if (key == null) {
            return;
        }

        if (object == null) {
            mAppPreferences.remove(key);
        } else if (object instanceof String) {
            mAppPreferences.put(key, (String) object);
        } else if (object instanceof Integer) {
            mAppPreferences.put(key, (Integer) object);
        } else if (object instanceof Boolean) {
            mAppPreferences.put(key, (Boolean) object);
        } else if (object instanceof Float) {
            mAppPreferences.put(key, (Float) object);
        } else if (object instanceof Long) {
            mAppPreferences.put(key, (Long) object);
        } else {
            mAppPreferences.put(key, object.toString());
        }
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object get(Context context, String key, Object defaultObject) {
        if (mAppPreferences == null) {
            mAppPreferences = new AppPreferences();
        }

        if (key == null) {
            return null;
        }

        if (defaultObject instanceof String) {
            return mAppPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return mAppPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return mAppPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return mAppPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return mAppPreferences.getLong(key, (Long) defaultObject);
        }

        return null;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.getAll();
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }

    static class AppPreferences extends TrayPreferences {

        private AppPreferences() {
            super(x.app(), "UserDefaults", 1);
        }

        @Override
        protected void onCreate(int initialVersion) {
            super.onCreate(initialVersion);

            migrate(new SharedPreferencesImport(getContext(), FILE_NAME, KEY_USER_INFO, KEY_USER_INFO),
                    new SharedPreferencesImport(getContext(), FILE_NAME, KEY_TOKEN, KEY_TOKEN),
                    new SharedPreferencesImport(getContext(), FILE_NAME, KEY_LAST_VERSION, KEY_LAST_VERSION),
                    new SharedPreferencesImport(getContext(), FILE_NAME, KEY_COMMUNITY_DATA, KEY_COMMUNITY_DATA),
                    new SharedPreferencesImport(getContext(), FILE_NAME, KEY_DEVICE_ID, KEY_DEVICE_ID),
                    new SharedPreferencesImport(getContext(), FILE_NAME, KEY_SHAKE_ENABLED, KEY_SHAKE_ENABLED),
                    new SharedPreferencesImport(getContext(), FILE_NAME, KEY_AUTO_DISCONNECT, KEY_AUTO_DISCONNECT),
                    new SharedPreferencesImport(getContext(), FILE_NAME, KEY_WIFI_SENSITIVITY, KEY_WIFI_SENSITIVITY),
                    new SharedPreferencesImport(getContext(), FILE_NAME, KEY_SCREEN_ON_ENABLED, KEY_SCREEN_ON_ENABLED));
        }
    }
}
