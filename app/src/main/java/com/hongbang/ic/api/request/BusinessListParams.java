package com.hongbang.ic.api.request;

import com.google.gson.annotations.SerializedName;
import com.hongbang.ic.api.annotation.Parameter;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * Created by xionghf on 16/4/21.
 */
@HttpRequest(
        path = HttpConstants.BUSINESS_LIST_PATH,
        builder = SessionParamsBuilder.class)
public class BusinessListParams extends RequestParams {

    public int type = 11;

    @Parameter("page")
    public int pageNo = 1;

    @Parameter("rows")
    public int pageLen = AppConstants.MAX_PAGE_LENGTH;

}
