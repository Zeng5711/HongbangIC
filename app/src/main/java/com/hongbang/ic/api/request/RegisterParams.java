package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * Created by xionghf on 16/4/2.
 */
@HttpRequest(
        path = HttpConstants.USER_REGISTER_PATH,
        builder = BaseParamsBuilder.class)
public class RegisterParams extends RequestParams {
    /**
     * 用户手机号
     */
    public String mobile;

    /**
     * 密码
     */
    public String pwd;

    public String captcha = "1";

    public String mobileId;
}