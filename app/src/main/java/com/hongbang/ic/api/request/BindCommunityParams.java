package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * Created by xionghf on 16/5/1.
 */
@HttpRequest(
        path = HttpConstants.USER_BIND_COMMUNITY_PATH,
        builder = SessionParamsBuilder.class)
public class BindCommunityParams extends RequestParams {

    public String parkId;

}