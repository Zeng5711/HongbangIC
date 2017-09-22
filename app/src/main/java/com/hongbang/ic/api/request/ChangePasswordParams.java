package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * 找回密码网络请求参数
 * <p>
 * Created by xionghf on 16/4/2.
 */
@HttpRequest(
        path = HttpConstants.USER_CHANGE_PWD_PATH,
        builder = SessionParamsBuilder.class)
public class ChangePasswordParams extends RequestParams {
    /**
     * 原密码
     */
    public String oldPwd;
    /**
     * 新密码
     */
    public String newPwd;

    public String captcha;

    public String mobileId;
}