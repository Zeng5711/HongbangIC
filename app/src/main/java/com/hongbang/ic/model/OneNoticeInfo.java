package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;

/**
 * 一条公告信息
 * <p>
 * Created by xionghf on 16/4/16.
 */
public class OneNoticeInfo {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("content")
    public String content;

    @SerializedName("date")
    public long date = -1;

}
