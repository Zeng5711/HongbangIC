package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xionghf on 16/4/10.
 */
public class OneCommunityInfo extends BaseCommunityChooseItem {

    @SerializedName("cityId")
    public String cityId;

    @SerializedName("code")
    public String code;

    @SerializedName("addr")
    public String addr;

    @SerializedName("picUrl")
    public String imageUrl;

    @SerializedName("picUrlBig")
    public String imageUrlLarge;

    @SerializedName("tel")
    public String telephone = "0431-12345678";

}
