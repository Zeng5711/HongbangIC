package com.hongbang.ic.common;

import com.hongbang.ic.api.request.AroundListParams;
import com.hongbang.ic.api.request.BindCommunityParams;
import com.hongbang.ic.api.request.BusinessListParams;
import com.hongbang.ic.api.request.ChangePasswordParams;
import com.hongbang.ic.api.request.CommitRepairParams;
import com.hongbang.ic.api.request.GetCaptchaParams;
import com.hongbang.ic.api.request.NoticeListParams;
import com.hongbang.ic.api.request.RepairListParams;
import com.hongbang.ic.api.request.RetrievePasswordParams;
import com.hongbang.ic.api.request.SessionParamsBuilder;
import com.hongbang.ic.api.request.ShareKeyParams;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.constant.HttpConstants;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.util.AppUtils;
import com.hongbang.ic.util.FileUtils;
import com.hongbang.ic.util.NetUtils;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

/**
 * 数据请求统一出口
 * <p/>
 * Created by xionghf on 16/4/24.
 */
public class DataManager {

    DataManager() {
    }

    /**
     * 绑定小区
     *
     * @param parkId   小区id
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable bindCommunity(String parkId, Callback.CommonCallback callback) {
        BindCommunityParams params = new BindCommunityParams();
        params.parkId = parkId;

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 获取主界面数据
     *
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getMainInfo(Callback.CommonCallback<?> callback) {
        RequestParams params = new RequestParams(getPath(HttpConstants.MAIN_INFO_PATH), new SessionParamsBuilder(), null, null);

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 设置用户信息
     *
     * @param image    头像文件地址
     * @param nickname 昵称
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable setUserInfo(String image, String nickname, Callback.CommonCallback callback) {
        RequestParams params = new RequestParams(getPath(HttpConstants.USER_UPDATE_INFO_PATH),
                new SessionParamsBuilder(), null, null);
        params.setMultipart(true);
        if (FileUtils.isFile(image)) {
            params.addBodyParameter("file", new File(image), "image/jpeg");
        }

        if (nickname != null && nickname.length() > 0) {
            params.addBodyParameter("name", nickname);
        }

        return invokeRequest(params, HttpMethod.POST, callback);
    }

    /**
     * 获取社区公告,社区新闻,服务指南列表
     *
     * @param type     列表类别 {@link com.hongbang.ic.constant.AppConstants#TYPE_COMMUNITY_NOTICE},
     *                 {@link com.hongbang.ic.constant.AppConstants#TYPE_SERVICE_GUIDE},
     *                 {@link com.hongbang.ic.constant.AppConstants#TYPE_COMMUNITY_NEWS}
     * @param pageNo   页号
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getNoticeList(int type, int pageNo, Callback.CommonCallback callback) {
        NoticeListParams params = new NoticeListParams();
        params.type = type;
        params.pageNo = pageNo;
        params.pageLen = AppConstants.MAX_PAGE_LENGTH;

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 获取周边列表
     *
     * @param type     列表类别 {@link com.hongbang.ic.constant.AppConstants#AROUND_CATEGORY_ID}
     * @param pageNo   页号
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getAroundList(int type, int pageNo, Callback.CommonCallback callback) {
        AroundListParams params = new AroundListParams();
        params.type = type;
        params.pageNo = pageNo;
        params.pageLen = AppConstants.MAX_PAGE_LENGTH;

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 获取商家列表
     *
     * @param type     列表类别 {@link com.hongbang.ic.constant.AppConstants#BUSINESS_CATEGORY_ID}
     * @param pageNo   页号
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getBusinessList(int type, int pageNo, Callback.CommonCallback callback) {
        BusinessListParams params = new BusinessListParams();
        params.type = type;
        params.pageNo = pageNo;
        params.pageLen = AppConstants.MAX_PAGE_LENGTH;
        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 获取报修列表
     *
     * @param pageNo   页号
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getRepairList(int pageNo, Callback.CommonCallback callback) {
        RepairListParams params = new RepairListParams();
        params.pageNo = pageNo;
        params.pageLen = AppConstants.MAX_PAGE_LENGTH;

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 获取报修详情
     *
     * @param id       报修id
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getRepairDetail(String id, Callback.CommonCallback callback) {
        RequestParams params = new RequestParams(getPath(HttpConstants.REPAIR_DETAIL_PATH),
                new SessionParamsBuilder(), null, null);
        params.addQueryStringParameter("id", id);

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 获取报修分类列表
     *
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getRepairTypeList(Callback.CommonCallback callback) {
        RequestParams params = new RequestParams(getPath(HttpConstants.REPAIR_TYPE_LIST_PATH),
                new SessionParamsBuilder(), null, null);

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 提交新报修
     *
     * @param type     保修分类
     * @param content  报修内容
     * @param image    报修图片
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable commitRepairInfo(String type, String content, String image, Callback.CommonCallback callback) {
        CommitRepairParams params = new CommitRepairParams();

        params.type = type;
        params.content = content;
        if (FileUtils.isFile(image)) {
            params.file = new File(image);
        }

        return invokeRequest(params, HttpMethod.POST, callback);
    }

    /**
     * 获取验证码
     *
     * @param mobile   手机号
     * @param type     验证码类别 {@link com.hongbang.ic.constant.AppConstants#CAPTCHA_USER_REGISTER},
     *                 {@link com.hongbang.ic.constant.AppConstants#CAPTCHA_RETRIEVE_PASSWORD},
     *                 {@link com.hongbang.ic.constant.AppConstants#CAPTCHA_CHANGE_PASSWORD}
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getCaptcha(String mobile,
                                          int type,
                                          Callback.CommonCallback callback) {
        return invokeRequest(new GetCaptchaParams(mobile, type), HttpMethod.GET, callback);
    }

    /**
     * 更新钥匙包
     *
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable getKeyList(Callback.CommonCallback callback) {
        RequestParams params = new RequestParams(getPath(HttpConstants.USER_GET_KEY_LIST_PATH),
                new SessionParamsBuilder(), null, null);

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 清除钥匙包
     *
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable clearKeyList(Callback.CommonCallback callback) {
        RequestParams params = new RequestParams(getPath(HttpConstants.USER_CLEAR_KEY_LIST_PATH),
                new SessionParamsBuilder(), null, null);

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 分享钥匙
     *
     * @param mobile   手机号
     * @param key      钥匙
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable shareKey(String mobile, OneKeyInfo4DB key, Callback.CommonCallback callback) {
        ShareKeyParams params = new ShareKeyParams();
        params.mobile = mobile;
//        params.key = key.tempKey + key.tempRoll;
        params.keyId = key.id;

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 找回密码
     *
     * @param mobile   手机号
     * @param password 密码
     * @param captcha  验证码
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable retrievePassword(String mobile,
                                                String password,
                                                String captcha,
                                                Callback.CommonCallback callback) {
        RetrievePasswordParams params = new RetrievePasswordParams();
        params.mobile = mobile;
        params.pwd = StringUtils.MD5(password);
        params.mobileId = AppUtils.getDeviceId(x.app());
        params.captcha = captcha;

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 修改密码
     *
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @param captcha     验证码
     * @param callback    回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable changePassword(String oldPassword,
                                              String newPassword,
                                              String captcha,
                                              Callback.CommonCallback callback) {
        ChangePasswordParams params = new ChangePasswordParams();
        params.oldPwd = StringUtils.MD5(oldPassword);
        params.newPwd = StringUtils.MD5(newPassword);
        params.mobileId = AppUtils.getDeviceId(x.app());
        params.captcha = captcha;

        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 发送请求
     *
     * @param params   请求参数 {@link org.xutils.http.RequestParams}
     * @param callback 回调函数
     * @return 取消句柄
     */
    public Callback.Cancelable invokeRequest(RequestParams params,
                                                     Callback.CommonCallback callback) {
        return invokeRequest(params, HttpMethod.GET, callback);
    }

    /**
     * 发送请求
     *
     * @param params   请求参数 {@link org.xutils.http.RequestParams}
     * @param method   请求方式 {@link org.xutils.http.HttpMethod}
     * @param callback 回调函数
     * @return 取消句柄
     */
    private Callback.Cancelable invokeRequest(RequestParams params,
                                                     HttpMethod method,
                                                     Callback.CommonCallback callback) {
        String error = null;
        if (!NetUtils.isConnected(x.app())) {
            error = "网络无法连接";
        } else if (UserDefaults.defaults().getUserInfo() != null) {
            String ssid = NetUtils.getConnectSSID(x.app());
            if (ssid != null && ssid.matches("^\"" +
                    UserDefaults.defaults().getUserInfo().communityId + "-000\"$")) {
                error = "正在连接发卡器，暂时无法访问网络";
            } else if (ssid != null && ssid.matches("^\"" +
                    UserDefaults.defaults().getUserInfo().communityId + "-\\d{3}\"$")) {
                error = "正在连接刷卡器，暂时无法访问网络";
            }

        }
        if (error != null) {
            T.showShort(x.app(), error);
            if (callback != null) {
                callback.onFinished();
            }
            return new Callback.Cancelable() {
                @Override
                public void cancel() {
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }
            };
        }
        if (method == HttpMethod.GET) {
            return x.http().get(params, callback);
        } else {
            return x.http().post(params, callback);
        }
    }

    private static String getPath(String path) {
        String host;
        if (x.isDebug()) {
            host = HttpConstants.SERVER_HOST_DEBUG;
        } else {
            host = HttpConstants.SERVER_HOST;
        }
        return host + "/" + path;
    }
}