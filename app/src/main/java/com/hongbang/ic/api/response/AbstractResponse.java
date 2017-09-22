package com.hongbang.ic.api.response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * 虚拟接口, 统一格式
 * <p>
 * Created by xionghf on 16/5/16.
 */
public abstract class AbstractResponse {

    public int code;

    public String msg;

    @SerializedName("sysDateTime")
    public long systemTime;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
