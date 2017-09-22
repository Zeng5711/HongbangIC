package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * Created by xionghf on 16/3/27.
 */
@HttpRequest(
        path = HttpConstants.USER_LOGIN_PATH,
        builder = BaseParamsBuilder.class)
public class LoginParams extends RequestParams {
    /**
     * 用户手机号
     */
    public String account;

    /**
     * 密码
     */
    public String password;

    public static final String osType = "android";

    public String deviceToken = "1";

    public String mobileId;

}