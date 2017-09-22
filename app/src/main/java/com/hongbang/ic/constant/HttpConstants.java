package com.hongbang.ic.constant;

/**
 * 网络请求地址常量文件
 * <p>
 * Created by xionghf on 16/3/26.
 */
public final class HttpConstants {
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    public static final String SERVER_DOMAIN = "123.56.199.2:8080";
    //    public static final String SERVER_DOMAIN = "192.168.31.204:8080";
    public static final String SERVER_HOST = "http://api.hongbangkeji.com/api";
    //    public static final String SERVER_HOST_DEBUG = HTTP + SERVER_DOMAIN + "/ICWeb/api";
//    public static final String SERVER_HOST_DEBUG = HTTP + SERVER_DOMAIN + "/api/api";
    public static final String SERVER_HOST_DEBUG = "http://api.hongbangkeji.com/api";
    public static final String SERVER_IMAGE = "http://www.hongbangkeji.com";


    /**
     * 用户相关接口
     */
    public static final String USER_ROOT_PATH = "user";
    /**
     * 登录
     */
    public static final String USER_LOGIN_PATH = USER_ROOT_PATH + "/login";
    /**
     * 注册
     */
    public static final String USER_REGISTER_PATH = USER_ROOT_PATH + "/register";
    /**
     * 找回密码
     */
    public static final String USER_RETRIEVE_PWD_PATH = USER_ROOT_PATH + "/resetPasswd";
    /**
     * 修改密码
     */
    public static final String USER_CHANGE_PWD_PATH = USER_ROOT_PATH + "/changePwd";
    /**
     * 获取验证码
     */
    public static final String USER_CAPTCHA_PATH = USER_ROOT_PATH + "/captcha";
    /**
     * 修改用户昵称, 用户头像
     */
    public static final String USER_UPDATE_INFO_PATH = USER_ROOT_PATH + "/update";
    /**
     * 绑定小区
     */
    public static final String USER_BIND_COMMUNITY_PATH = USER_ROOT_PATH + "/bindDefaultPark";
    /**
     * 获取钥匙列表
     */
    public static final String USER_GET_KEY_LIST_PATH = USER_ROOT_PATH + "/getUserKey";
    /**
     * 获取钥匙列表
     */
    public static final String USER_CLEAR_KEY_LIST_PATH = USER_ROOT_PATH + "/delUserKey";
    /**
     * 绑定小区
     */
    public static final String USER_SHARE_KEY = USER_ROOT_PATH + "/pubKey";


    /**
     * 获取主界面信息
     */
    public static final String MAIN_INFO_PATH = "main/mainData";


    /**
     * 获取城市列表
     */
    public static final String CITY_LIST_PATH = "city/getCity";
    /**
     * 获取小区列表
     */
    public static final String COMMUNITY_LIST_PATH = "park/list";


    /**
     * 公告,新闻,服务指南
     */
    public static final String NOTICE_ROOT_PATH = "notice";
    /**
     * 公告,新闻,服务指南 列表
     */
    public static final String NOTICE_LIST_PATH = NOTICE_ROOT_PATH + "/list";
    /**
     * 公告,新闻,服务指南 详情
     */
    public static final String NOTICE_DETAIL_PATH = NOTICE_ROOT_PATH + "/getInfo";


    /**
     * 周边列表
     */
    public static final String AROUND_LIST_PATH = "near/list";
    /**
     * 周边列表
     */
    public static final String AROUND_DETAIL_PATH = "near/getNearInfo";
    /**
     * 商家列表
     */
    public static final String BUSINESS_LIST_PATH = "adv/list";
    /**
     * 商家详情
     */
    public static final String BUSINESS_DETAIL_PATH = "adv/getAdvInfo";


    /**
     * 报修相关接口
     */
    public static final String REPAIR_ROOT_PATH = "repair";
    /**
     * 报修列表
     */
    public static final String REPAIR_LIST_PATH = REPAIR_ROOT_PATH + "/list";
    /**
     * 报修详情
     */
    public static final String REPAIR_DETAIL_PATH = REPAIR_ROOT_PATH + "/getRepairInfo";
    /**
     * 报修类型
     */
    public static final String REPAIR_TYPE_LIST_PATH = REPAIR_ROOT_PATH + "/getType";
    /**
     * 提交新报修
     */
    public static final String REPAIR_COMMIT_PATH = REPAIR_ROOT_PATH + "/save";

}
