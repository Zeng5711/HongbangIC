package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * Created by xionghf on 16/4/10.
 */
@HttpRequest(
        path = HttpConstants.COMMUNITY_LIST_PATH,
        builder = SessionParamsBuilder.class)
public class GetCommunityListParams extends RequestParams {

    public String cityId;

}
