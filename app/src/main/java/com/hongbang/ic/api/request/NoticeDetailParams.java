package com.hongbang.ic.api.request;

import com.hongbang.ic.constant.HttpConstants;

import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpRequest;

/**
 * 公告详情参数
 * <p>
 * Created by xionghf on 16/5/23.
 */
@HttpRequest(
        path = HttpConstants.NOTICE_DETAIL_PATH,
        builder = SessionParamsBuilder.class)
public class NoticeDetailParams extends RequestParams {

    public String id;

    public NoticeDetailParams(String id) {
        this.id = id;
    }
}
