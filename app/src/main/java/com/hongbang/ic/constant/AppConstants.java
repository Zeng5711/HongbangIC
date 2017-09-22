package com.hongbang.ic.constant;

import android.util.SparseArray;

import com.hongbang.ic.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用常量
 * <p/>
 * Created by xionghf on 16/4/5.
 */
public class AppConstants {

    public static final int[] AROUND_CATEGORY_ICONS = {R.drawable.sel_meishiwaimai, R.drawable.sel_baihuoshangjia,
            R.drawable.sel_bianmingfuwu, R.drawable.sel_jiazhengfuwu,
            R.drawable.sel_fangchanzhongjie, R.drawable.sel_meirongjianshen,
            R.drawable.sel_xiuxianyule, R.drawable.sel_muyinghaitong,
            R.drawable.sel_jiaoyupeixun, R.drawable.sel_jiudianyuding};
    public static final String[] AROUND_CATEGORY_NAMES = {"美食外卖", "百货商家", "便民服务", "家政服务",
            "房产中介", "美容健身", "休闲娱乐", "母婴孩童", "教育培训", "酒店预订"};
    public static final int[] AROUND_CATEGORY_ID = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    public static final int[] BUSINESS_CATEGORY_ICONS = {R.drawable.sel_shumajiadian, R.drawable.sel_fushimeirong,
            R.drawable.sel_canyinyule, R.drawable.sel_qicheshanglv,
            R.drawable.sel_muyinghaitong, R.drawable.sel_yundongjianshen,
            R.drawable.sel_bangongyongping, R.drawable.sel_wangdianzhixiao,
            R.drawable.sel_xiuxianshiping, R.drawable.sel_chuxinglvyou};
    public static final String[] BUSINESS_CATEGORY_NAMES = {"数码家电", "服饰美容", "娱乐餐饮", "汽车商旅",
            "母婴孩童", "运动健康", "办公用品", "网店直销", "休闲食品", "出行旅游"};

    public static final int[] BUSINESS_CATEGORY_ID = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

    private static final int STATE_UNPROCESSED = 0;
    private static final int STATE_PROCESSING = 1;
    private static final int STATE_PROCESSED = 2;

    public static final SparseArray<Integer> PROCESS_STATE_ICON_MAP = new SparseArray<>();

    static {
        PROCESS_STATE_ICON_MAP.append(STATE_UNPROCESSED, R.drawable.icon_corner_unprocessed);
        PROCESS_STATE_ICON_MAP.append(STATE_PROCESSING, R.drawable.icon_corner_processing);
        PROCESS_STATE_ICON_MAP.append(STATE_PROCESSED, R.drawable.icon_corner_processed);
    }

    public static final String DATE_FORMAT_1 = "yyyy-MM-dd HH:mm";

    public static final int MAX_PAGE_LENGTH = 20;

    /**
     * 社区公告
     */
    public static final int TYPE_COMMUNITY_NOTICE = 1;
    /**
     * 服务指南
     */
    public static final int TYPE_SERVICE_GUIDE = 2;
    /**
     * 社区新闻
     */
    public static final int TYPE_COMMUNITY_NEWS = 3;


    /**
     * 验证码类型:找回密码
     */
    public static final int CAPTCHA_RETRIEVE_PASSWORD = 1;
    /**
     * 验证码类型:用户注册
     */
    public static final int CAPTCHA_USER_REGISTER = 2;
    /**
     * 验证码类型:修改密码
     */
    public static final int CAPTCHA_CHANGE_PASSWORD = 3;
    /**
     * 获取验证码时间间隔, 秒
     */
    public static final int CAPTCHA_GET_INTERVAL = 60;


    /**
     * 微信分享appID
     */
    public static final String WEIXIN_APP_ID = "wx38df07550cba5499";
    /**
     * 微信分享网页地址
     */
    public static final String SHARE_URL = "http://www.sy-card.com";
    /**
     * 微信分享标题
     */
    public static final String SHARE_TITLE = "智慧社区，和谐生活";
    /**
     * 微信分享标题
     */
    public static final String SHARE_DESCRIPTION = "把服务做到家，一站式社区服务平台";

    /**
     * 热线电话
     */
    public static final String HOTLINE_NUMBER = "024-31258269";
    /**
     * 热线电话
     */
    public static final String MANUAL_URL = "http://help.hongbangkeji.com";

    /**
     *
     */
    public static final int BRUSH_CARD_RETRY_INTERVAL = 2000;

    /**
     * 普通
     */
    public static final int KEY_TYPE_NORMAL = 0;

    /**
     * 全通
     */
    public static final int KEY_TYPE_ALL = 1;

    /**
     * 计次
     */
    public static final int KEY_TYPE_TIMES = 2;

    /**
     * 储值
     */
    public static final int KEY_TYPE_STORED = 3;

    /**
     * 临时
     */
    public static final int KEY_TYPE_SHARED = 4;
}
