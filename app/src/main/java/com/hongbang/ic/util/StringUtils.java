package com.hongbang.ic.util;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String工具类
 * <p/>
 * Created by xionghf on 16/4/2.
 */
public class StringUtils {

    public static boolean isPhone(String phone) {
        Pattern pattern = Pattern.compile("^1[34578]\\d{9}$");
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    public static boolean isCaptcha(String captcha) {
        Pattern pattern = Pattern.compile("^\\d{6}$");
        Matcher matcher = pattern.matcher(captcha);
        return matcher.matches();
    }

    public static boolean isInet4Address(String s) {
        Pattern pattern = Pattern.compile(
                "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
        Matcher matcher = pattern.matcher(s);
        if (matcher.matches()) {
            for (int i = 0; i < 4; i++) {
                int v = Integer.valueOf(matcher.group(i + 1));
                if (v < 0 || v > 255) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String MD5(String s) {
        if (isEmpty(s)) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatDate(long time, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        String result;
        try {
            result = format.format(new Date(time));
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    public static byte[] hex2Bytes(String hexString) {
        if (hexString == null || hexString.length() < 2) {
            return null;
        }

        int length = hexString.length() / 2;
        byte[] retval = new byte[length];
        for (int i = 0; i < length; i++) {
            retval[i] = hex2Byte(hexString.substring(2 * i, 2 * i + 2).toUpperCase());
        }
        return retval;
    }

    public static String bytes2Hex(byte[] data) {
        if (data == null || data.length < 1) {
            return null;
        }

        final StringBuilder stringBuilder = new StringBuilder(data.length * 2);
        for (byte b : data) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }

    public static String bytes2Hex(byte[] data, int start, int length) {
        if (data == null || data.length < start + length || length <= 0) {
            return null;
        }

        final StringBuilder stringBuilder = new StringBuilder(data.length * 2);
        for (int i = start; i < start + length; i++) {
            stringBuilder.append(String.format("%02X", data[i]));
        }
        return stringBuilder.toString();
    }

    public static String bytes2Hex2(byte[] data, int start, int length) {
        if (data == null || data.length < start + length || length <= 0) {
            return null;
        }

        final StringBuilder stringBuilder = new StringBuilder(data.length * 2);
        for (int i = start; i < start + length; i++) {
            stringBuilder.append(String.format("%02X ", data[i]));
        }
        return stringBuilder.toString().trim();
    }

    private static final String HEX_CHARS = "0123456789ABCDEF";

    private static byte hex2Byte(String hex) {
        return (byte) ((HEX_CHARS.indexOf(hex.charAt(0)) * 16
                + HEX_CHARS.indexOf(hex.charAt(1))) & 0xff);
    }

    public static Date string2Date(String time, String pattern) {
        if (isEmpty(time) || isEmpty(pattern)) {
            return null;
        }
        if (time.length() > 16) {
            time = time.substring(0, 16);
        }

        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.CHINA);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        Date date;
        try {
            date = formatter.parse(time);
        } catch (ParseException e) {
            date = null;
        }
        return date;
    }

}
