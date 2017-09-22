package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * Created by xionghf on 16/4/10.
 */
@HttpRequest(
        path = HttpConstants.CITY_LIST_PATH,
        builder = SessionParamsBuilder.class)
public class GetCityListParams extends RequestParams {

    public String id;

}
