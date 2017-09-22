package com.hongbang.ic.util;

import java.io.File;

/**
 * Created by xionghf on 16/5/1.
 */
public class FileUtils {

    public static boolean isFile(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        File f = new File(path);
        return f.exists() && f.isFile();
    }

    public static boolean isDirectory(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }

    public static boolean delete(String path) {
        if (isFile(path) || isDirectory(path)) {
            return new File(path).delete();
        } else {
            return true;
        }
    }

}
