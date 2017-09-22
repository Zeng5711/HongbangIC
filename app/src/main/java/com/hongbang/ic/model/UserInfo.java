package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;

/**
 * 用户信息
 * <p/>
 * Created by xionghf on 16/5/14.
 */
public class UserInfo {

    @SerializedName("id")
    public String id;

    @SerializedName("nickName")
    public String nickname;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("picUrl")
    public String headPortrait;

    @SerializedName("picUrlBig")
    public String headPortraitLarge;

    @SerializedName("roleName")
    public String role;

    @SerializedName("parkId")
    public String communityId;

    @SerializedName("communityName")
    public String communityName;

}
