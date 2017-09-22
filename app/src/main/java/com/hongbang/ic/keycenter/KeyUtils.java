package com.hongbang.ic.keycenter;

import android.os.SystemClock;

import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.model.OneKeyInfo;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.util.StringUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * 钥匙工具
 * <p/>
 * Created by xionghf on 16/6/2.
 */
public class KeyUtils {

    private static final int MAX_RETRY_COUNT = 3;

    public static int clearTempKeys() {
        if (UserDefaults.defaults().getUserInfo() == null) {
            return 0;
        }

        Calendar today = Calendar.getInstance();
        long time = today.getTimeInMillis();
        DbManager db = x.getDb(app.daoConfig());

        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            try {
                List<OneKeyInfo4DB> list = db.selector(OneKeyInfo4DB.class)
                        .where("community_id", "=", UserDefaults.defaults().getUserInfo().communityId)
                        .and("period", "<", time)
                        .and("type", "in", new int[]{AppConstants.KEY_TYPE_NORMAL,
                                AppConstants.KEY_TYPE_ALL, AppConstants.KEY_TYPE_SHARED})
                        .findAll();
                if (list != null && list.size() > 0) {
                    db.delete(list);
                    return list.size();
                }
                return 0;
            } catch (DbException e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    try {
                        db.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            SystemClock.sleep(100);
        }
        return 0;
    }

    public static List<OneKeyInfo4DB> getAll() {
        if (UserDefaults.defaults().getUserInfo() == null) {
            return null;
        }
        DbManager db = null;
        List<OneKeyInfo4DB> keyList = null;

        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            try {
                if (UserDefaults.defaults().getUserInfo() != null) {
                    db = x.getDb(app.daoConfig());
                    keyList = db.selector(OneKeyInfo4DB.class)
                            .where("community_id", "=", UserDefaults.defaults().getUserInfo().communityId)
                            .orderBy("type", true)
                            .orderBy("_id", true)
                            .findAll();
                } else {
                    keyList = null;
                }
                break;
            } catch (DbException e) {
                e.printStackTrace();
                keyList = null;
            } finally {
                if (db != null) {
                    try {
                        db.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            SystemClock.sleep(100);
        }

        return keyList;
    }

    public static boolean update(OneKeyInfo4DB keyInfo) {
        DbManager db = null;
        if (keyInfo == null) {
            return false;
        }
        boolean result = false;
        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            try {
                db = x.getDb(app.daoConfig());
                db.saveOrUpdate(keyInfo);
                result = true;
                break;
            } catch (DbException e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    try {
                        db.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            SystemClock.sleep(100);
        }

        return result;
    }

    public static boolean remove(OneKeyInfo4DB keyInfo) {
        DbManager db = null;
        if (keyInfo == null) {
            return false;
        }
        boolean result = false;
        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            try {
                db = x.getDb(app.daoConfig());
                db.delete(keyInfo);
                result = true;
                break;
            } catch (DbException e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    try {
                        db.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            SystemClock.sleep(100);
        }

        return result;
    }

    public static void saveAll(List<OneKeyInfo4DB> keyList) {
        DbManager db = null;
        List<OneKeyInfo4DB> oldKeyList = getAll();

        if (oldKeyList == null) {
            oldKeyList = new ArrayList<>();
        }

        int flag = oldKeyList.size();

        if (keyList != null && keyList.size() > 0) {
            oldKeyList.addAll(keyList);
        }

        if (oldKeyList.size() == 0) {
            return;
        }

        int index = 0, i = 0, j;
        for (OneKeyInfo4DB key : oldKeyList) {
            boolean keep = true;
            j = 0;
            for (OneKeyInfo4DB key2 : oldKeyList) {
                if (key == key2) {
                    continue;
                }
                if (key.equals(key2) && i < j) {
                    keep = false;
                    break;
                } else if (key.type != AppConstants.KEY_TYPE_SHARED && key2.type != AppConstants.KEY_TYPE_SHARED
                        && compareKeyId(key.id, key2.id) < 0) {
                    keep = false;
                    break;
                }
                j++;
            }
            if (keep && index >= flag) {
                update(key);
            } else if (!keep && index < flag) {
                remove(key);
            }
            index++;
            i++;
        }
    }

    private static int compareKeyId(String id1, String id2) {
        try {
            return Integer.valueOf(id1) - Integer.valueOf(id2);
        } catch (Exception e) {
            return 0;
        }

    }

}
