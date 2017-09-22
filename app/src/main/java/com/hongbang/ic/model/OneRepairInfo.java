package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 一条报修信息
 * <p/>
 * Created by xionghf on 16/4/9.
 */
public class OneRepairInfo {

    @SerializedName("id")
    public String id;

    @SerializedName("type")
    public String title;

    @SerializedName("startDate")
    public long submitTime = -1;

    @SerializedName("content")
    public String content;

    @SerializedName("status")
    public int processState = 0;

    @SerializedName("picPath")
    public String imageUrl = null;

    @SerializedName("endDate")
    public long feedbackTime = -1;

    @SerializedName("answer")
    public String feedback = null;

}