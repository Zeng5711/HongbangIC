package com.hongbang.ic.common;

import android.app.Application;

import com.hongbang.ic.model.OneKeyInfo4DB;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

/**
 * 工具类
 * <p/>
 * Created by xionghf on 16/5/13.
 */
public class app {

    private DataManager dataManager;

    private static app appImpl;

    private app() {
    }

    public static DataManager data() {
        if (app.Inner.dataManager == null) {
            synchronized (DataManager.class) {
                if (app.Inner.dataManager == null) {
                    app.Inner.dataManager = new DataManager();
                }
            }
        }
        return app.Inner.dataManager;
    }

    public static DbManager.DaoConfig daoConfig() {
        if (app.Inner.daoConfig == null) {
            synchronized (DataManager.class) {
                if (app.Inner.daoConfig == null) {
                    app.Inner.daoConfig = new DbManager.DaoConfig()
                            .setDbName("hongbang.db")
                            .setDbVersion(2)
                            .setDbOpenListener(new DbManager.DbOpenListener() {
                                @Override
                                public void onDbOpened(DbManager db) {
                                    db.getDatabase().enableWriteAheadLogging();
                                }
                            })
                            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                                @Override
                                public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                                }
                            });
                }
            }
        }
        return app.Inner.daoConfig;
    }

    public static class Inner {
        private static Application app;
        private static DataManager dataManager;
        private static DbManager.DaoConfig daoConfig;

        private Inner() {
        }

        public static void init(Application app) {
            if (Inner.app == null) {
                Inner.app = app;
            }
        }

    }

}
