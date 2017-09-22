package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * 分享钥匙参数
 * <p>
 * Created by xionghf on 16/4/21.
 */
@HttpRequest(
        path = HttpConstants.USER_SHARE_KEY,
        builder = SessionParamsBuilder.class)
public class ShareKeyParams extends RequestParams {

    public String mobile;

    public String keyId;

}
