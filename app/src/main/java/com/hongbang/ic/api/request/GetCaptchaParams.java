package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * Created by xionghf on 16/4/2.
 */
@HttpRequest(
        path = HttpConstants.USER_CAPTCHA_PATH,
        builder = BaseParamsBuilder.class)
public class GetCaptchaParams extends RequestParams {
    /**
     * 用户手机号
     */
    public String mobile;

    /**
     * 验证码类型
     */
    public int type;

    public GetCaptchaParams(String mobile, int type) {
        this.mobile = mobile;
        this.type = type;
    }
}
