package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;

/**
 * 一条商家信息
 * <p/>
 * Created by xionghf on 16/4/16.
 */
public class OneBusinessInfo {
    @SerializedName("id")
    public String id;

    @SerializedName("picUrl")
    public String imageUrl;

    @SerializedName("title")
    public String title;

    @SerializedName("shortContent")
    public String shortContent;

    @SerializedName("addr")
    public String address;

}
