package com.hongbang.ic.api.request;

import com.hongbang.ic.common.UserDefaults;

import org.xutils.http.RequestParams;

/**
 * 参数构造器 带token
 * <p>
 * Created by xionghf on 16/4/10.
 */
public class SessionParamsBuilder extends BaseParamsBuilder {

    @Override
    public void buildParams(RequestParams params) {
        params.addQueryStringParameter("token", UserDefaults.defaults().getToken());
        super.buildParams(params);
    }

}
