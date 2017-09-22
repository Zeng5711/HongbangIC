package com.hongbang.ic.api.response;

import org.xutils.http.annotation.HttpResponse;

/**
 * 成功失败用的返回值
 * <p>
 * Created by xionghf on 16/4/2.
 */
@HttpResponse(parser = JsonResponseParser.class)
public class DefaultResponse extends AbstractResponse {
    
}
