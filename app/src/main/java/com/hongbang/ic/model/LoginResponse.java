package com.hongbang.ic.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xionghf on 16/3/27.
 */
public class LoginResponse {

    @SerializedName("token")
    public String token;

    @SerializedName("nickName")
    public String nickname;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("picUrl")
    public String headPortrait;

    @SerializedName("roleName")
    public String role;

    @SerializedName("parkId")
    public String communityId;

}
