package com.hongbang.ic.api.response;

import com.google.gson.annotations.SerializedName;
import com.hongbang.ic.constant.AppConstants;

import org.xutils.http.annotation.HttpResponse;

/**
 * 正常包含数据与的返回值
 * <p/>
 * Created by xionghf on 16/3/27.
 */
@HttpResponse(parser = JsonResponseParser.class)
public class BaseResponse<T> extends AbstractResponse {

    public int total = 0;

    @SerializedName("page")
    public int pageNo;

    @SerializedName("rows")
    public int pageLen;

    private T data;

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
