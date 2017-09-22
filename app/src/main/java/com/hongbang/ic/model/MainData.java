package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;

/**
 * 主界面数据
 * <p/>
 * Created by xionghf on 16/5/14.
 */
public class MainData {

    @SerializedName("park")
    public OneCommunityInfo communityInfo;

    @SerializedName("adv")
    public OneAdInfo adv;

    @SerializedName("notice")
    public OneNoticeInfo notice;

    @SerializedName("userCount")
    public int registerCount = 0;
}
