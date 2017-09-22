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
        path = HttpConstants.USER_RETRIEVE_PWD_PATH,
        builder = BaseParamsBuilder.class)
public class RetrievePasswordParams extends RequestParams {
    /**
     * 用户手机号
     */
    public String mobile;

    /**
     * 密码
     */
    public String pwd;

    public String captcha;

    public String mobileId;
}